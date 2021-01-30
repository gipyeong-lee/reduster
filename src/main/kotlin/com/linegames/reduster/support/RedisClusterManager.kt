package com.linegames.reduster.support

import com.google.common.collect.Ordering
import com.linegames.reduster.domain.ValueComparableMap
import com.linegames.reduster.util.JumpConsistentHash
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.concurrent.thread
import kotlin.math.abs

@Component
class RedisClusterManager {
    var hashTables: ValueComparableMap<String, Int> = ValueComparableMap(Ordering.natural())
    var buckets: ConcurrentSkipListMap<Int, StatefulRedisConnection<String, String>> =
        ConcurrentSkipListMap(Comparator.naturalOrder())
    var weight: Int = 10
    fun mset(map: Map<String, String>): Map<String, String> {
        map.keys.forEach { key ->
            val valueKey = JumpConsistentHash.hash(key)
            val serverKey = searchServerKeyByHash(valueKey)
            buckets[serverKey]!!.sync().set(key, map[key])
            hashTables[key] = valueKey
        }
        return map
    }

    fun mget(keys: Set<String>): Map<String, String?> {
        var mutableMap = mutableMapOf<String, String?>()
        keys.forEach { key ->
            val valueKey = JumpConsistentHash.hash(key)
            val serverKey = searchServerKeyByHash(valueKey)
            mutableMap[key] = buckets[serverKey]!!.sync().get(key)
        }
        return mutableMap
    }

    fun set(key: String, value: String): String? {
        val valueKey = JumpConsistentHash.hash(key)
        val serverKey = searchServerKeyByHash(valueKey)
        buckets[serverKey]!!.sync().set(key, value)
        hashTables[key] = valueKey
        return value
    }

    fun get(key: String): String? {
        return command(key)?.get(key)
    }

    fun insert(host: String, port: Int): String {
        val uri = makeUri(host, port)
        var list: MutableList<Thread> = mutableListOf()
        for (i in 1..weight) {
            val thread = thread {
                val key = JumpConsistentHash.hash("${uri}-${i}")
                if (!buckets.keys.contains(key)) {
                    val connection = RedisClient.create(RedisURI.create(host, port)).connect()
                    connection.timeout = Duration.ofSeconds(3)
                    connection.sync()?.ping()

                    buckets[key] = connection
                    var targetIdx = buckets.keys.indexOf(key)
                    if (targetIdx < 0) {
                        targetIdx = 0
                    }
                    var sourceIdx = targetIdx + 1
                    if (sourceIdx >= buckets.keys.size) {
                        sourceIdx = 0
                    }
                    if (sourceIdx == targetIdx) {
                        return@thread
                    }
                    val targetServer = buckets[key]?.sync()
                    val sourceServer = buckets[buckets.keys.elementAt(sourceIdx)]?.sync()
                    // remove key from buckets first
                    // find values
                    val move = hashTables.filter { key - it.value!! > 0 }
                    move.keys.forEach { _key ->
                        val _value = sourceServer?.get(_key)
                        targetServer?.set(_key, _value)
                    }
                }
            }
            list.add(thread)
        }
        list.map {
            it.join()
        }
        return "insert"
    }

    fun delete(host: String, port: Int): String {
        val uri = makeUri(host, port)
        var list: MutableList<Thread> = mutableListOf()
        for (i in 1..weight) {
            val thread = thread {
                val key = JumpConsistentHash.hash("${uri}-${i}") // serverKey
                var sourceIdx = buckets.keys.indexOf(key)
                if (sourceIdx < 0) {
                    sourceIdx = 0
                }
                var targetIdx = sourceIdx + 1
                if (targetIdx >= buckets.keys.size) {
                    targetIdx = 0
                }
                if (sourceIdx == targetIdx) {
                    return@thread
                }
                val sourceServer = buckets[key]
                val targetServer = buckets[buckets.keys.elementAt(targetIdx)]?.sync()
                // remove key from buckets first
                // find values
                val move = hashTables.filter { key - it.value!! > 0 }
                move.keys.forEach { _key ->
                    val _value = sourceServer?.sync()?.get(_key)
                    targetServer?.set(_key, _value)
                }
                if(key != 1){
                    buckets.remove(key)
                }
            }
            list.add(thread)
        }
        list.map {
            it.join()
        }
        // close after all keys moved.
        val key = JumpConsistentHash.hash("${uri}-1") // serverKey
        buckets[key]?.close()
        buckets.remove(key)

        return "delete"
    }

    fun calculateHash(key: String): Long {
        return abs(key.hashCode().toLong())
    }

    fun makeUri(host: String, port: Int): String {
        return "redis://${host}:${port}"
    }

    fun searchServerKey(key: String): Int {
        val valueKey = JumpConsistentHash.hash(key)
        return searchServerKeyByHash(valueKey)
    }

    fun searchServerKeyByHash(hash: Int): Int {
        val keys = buckets.keys
        return keys.find { _key ->
            _key - hash > 0
        } ?: keys.first()
    }

    fun command(key: String): RedisCommands<String, String>? {
        val serverKey = searchServerKey(key)
        return buckets[serverKey]!!.sync()
    }
}
