package com.indiemove.reduster.manager

import com.indiemove.reduster.support.RedisClusterManager
import com.palantir.docker.compose.DockerComposeRule
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class RedisClusterManagerTest {
    @ClassRule
    var docker = DockerComposeRule.builder()
        .file("docker-compose.yml")
        .saveLogsTo("build/dockerLogs/dockerComposeRuleTest")
        .build()
    private val redisClusterManager: RedisClusterManager = RedisClusterManager()

    init {
        redisClusterManager.insert("127.0.0.1", 6379)
        redisClusterManager.insert("127.0.0.1", 6380)
        redisClusterManager.insert("127.0.0.1", 6381)
        redisClusterManager.insert("127.0.0.1", 6382)
    }

    @Test
    fun `set`() {
        redisClusterManager.set("hello", "world")
        assertEquals(redisClusterManager.get("hello")!!, "world")
    }

    @Test
    fun `mset 1 to 100000`() {
        var count = 100000
        var data = mutableMapOf<String,String>()
        var keys = mutableSetOf<String>()
        (1..count).forEach { down ->
            data.put("mset$down","mset$down")
            keys.add("mset$down")
        }
        redisClusterManager.mset(data)
        val result = redisClusterManager.mget(keys)

        assertEquals(count, result.size)
    }
}
