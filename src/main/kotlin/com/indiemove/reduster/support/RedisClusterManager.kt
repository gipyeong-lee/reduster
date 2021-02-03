package com.indiemove.reduster.support

import com.google.common.collect.Ordering
import com.indiemove.reduster.domain.ValueComparableMap
import com.indiemove.reduster.util.JumpConsistentHash
import io.lettuce.core.KeyValue
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.abs

@Component
class RedisClusterManager : IClusterManager {
    var hashTables: ValueComparableMap<String, Int> = ValueComparableMap(Ordering.natural())
    var buckets: ConcurrentSkipListMap<Int, StatefulRedisConnection<String, String>> =
        ConcurrentSkipListMap(Comparator.naturalOrder())
    var weight: Int = 10
    override fun mset(map: Map<String, String>): Map<String, String> {
        val commander = ConcurrentSkipListMap<Int, MutableMap<String, String>>()
        map.entries.forEach { (key, value) ->
            val valueKey = JumpConsistentHash.hash(key)
            val serverKey = searchServerKeyByHash(valueKey)
            if (commander[serverKey] == null) {
                commander[serverKey] = mutableMapOf()
            }
            commander[serverKey]?.put(key, value)
            hashTables[key] = valueKey
        }
        commander.entries.forEach { (serverKey, data) ->
            buckets[serverKey]!!.sync().mset(data)
        }

        return map
    }

    override fun mget(keys: Set<String>): List<KeyValue<String, String>> {
        var mutableList = Collections.synchronizedList(mutableListOf<KeyValue<String, String>>())
        val commander = ConcurrentSkipListMap<Int, MutableSet<String>>()
        keys.forEach { key ->
            val valueKey = JumpConsistentHash.hash(key)
            val serverKey = searchServerKeyByHash(valueKey)
            if (commander[serverKey] == null) {
                commander[serverKey] = mutableSetOf()
            }
            commander[serverKey]?.add(key)
        }
        commander.entries.forEach { (serverKey, keys) ->
            mutableList.addAll(buckets[serverKey]!!.sync().mget(*keys.toTypedArray()))
        }
        return mutableList
    }

    override fun set(key: String, value: String): String? {
        val valueKey = JumpConsistentHash.hash(key)
        val serverKey = searchServerKeyByHash(valueKey)
        buckets[serverKey]!!.sync().set(key, value)
        hashTables[key] = valueKey
        return value
    }

    override fun get(key: String): String? {
        return command(key)?.get(key)
    }

    override fun insert(host: String, port: Int) {
        val uri = makeUri(host, port)
        for (i in 1..weight) {
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
                    continue
                }
                var sourceServerKey = buckets.keys.elementAt(sourceIdx)
                val targetServer = buckets[key]?.sync()
                val sourceServer = buckets[sourceServerKey]?.sync()
                val move = hashTables.filter { key - it.value!! > 0 }
                val targetData = mutableMapOf<String, String>()
                if (move.isNotEmpty()) {
                    sourceServer?.mget(*move.keys.toTypedArray())?.forEach { data ->
                        targetData[data.key] = data.value
                    }
                    targetServer?.mset(targetData)
                }
            }
        }
    }

    override fun delete(host: String, port: Int) {
        val uri = makeUri(host, port)
        for (i in 1..weight) {
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
                continue
            }
            val sourceServer = buckets[key]
            val targetServer = buckets[buckets.keys.elementAt(targetIdx)]?.sync()
            val move = hashTables.filter { key - it.value!! > 0 }

            if (move.isNotEmpty()) {
                val targetData = mutableMapOf<String, String>()
                sourceServer?.sync()?.mget(*move.keys.toTypedArray())?.filter { data -> data.hasValue() }
                    ?.forEach { data ->
                        targetData[data.key] = data.value
                    }
                targetServer?.mset(targetData)
            }
            if (i != 1) {
                buckets.remove(key)
            }
        }
        val key = JumpConsistentHash.hash("${uri}-${1}") // serverKey
        buckets[key]?.close()
        buckets.remove(key)
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
