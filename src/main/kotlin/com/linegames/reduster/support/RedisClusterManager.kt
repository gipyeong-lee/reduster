package com.linegames.reduster.support

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.math.abs

@Component
class RedisClusterManager {
    var servers: MutableMap<String, StatefulRedisConnection<String, String>> = mutableMapOf()
    var degreeTables: HashMap<String, Float> = hashMapOf()
    var weight: Int = 10
    fun setValue(key: String, value: String): String? {
        command(key)?.set(key, value)
        degreeTables[key] = calculateDegree(key)
        return command(key)?.get(key)
    }

    fun getValue(key: String): String? {
        return command(key)?.get(key)
    }

    fun insert(host: String, port: Int): String {
        val uri = "redis://${host}:${port}/0"
        val key = calculateHash(uri).toString()
        if (servers.keys.contains(key)) {
            try {
                servers[key]?.sync()?.ping()
            } catch (e: Exception) {
                servers.remove(key)
                return "unknown"
            }
            return "exist"
        }
        try {
            val connection = RedisClient.create(uri).connect()
            connection.timeout = Duration.ofSeconds(3)
            connection.sync()?.ping()
            servers[key] = connection
            degreeTables[key] = calculateDegree(key)
        } catch (e: Exception) {
            return "unknown"
        }
        return "insert"
    }

    fun delete(host: String, port: Int): String {
        val uri = "redis://${host}:${port}/0"
        val key = calculateHash(uri).toString()
        if (!servers.keys.contains(key)) {
            return "unknown"
        }
        servers[key]?.close()
        servers.remove(key)
        return "delete"
    }

    fun calculateHash(key: String): Long {
        return abs(key.hashCode().toLong())
    }

    fun calculateDegree(key: String): Float {
        return calculateHash(key) % 360.0f
    }

    fun calculateDegree(hash: Long): Float {
        return hash % 360.0f
    }

    fun searchServerKey(key: String): String? {
        val degree = calculateDegree(key)
        return servers.keys.find { k -> calculateDegree(calculateHash(k)) < degree }
    }

    fun command(key: String): RedisCommands<String, String>? {
        val serverKey = searchServerKey(key)
        return servers[serverKey]?.sync()
    }
}
