package com.indiemove.reduster.controller

import com.indiemove.reduster.domain.ApiResponse
import com.indiemove.reduster.domain.CommandResponse
import com.indiemove.reduster.support.RedisClusterManager
import com.indiemove.reduster.util.JumpConsistentHash
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/redis/cmd")
@CrossOrigin(origins = ["*"])
class RedisCommandController(val redisClusterManager: RedisClusterManager) {
    @GetMapping(
        "/get/{key}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun get(@PathVariable key: String): CommandResponse {
        val value = redisClusterManager.get(key)
        val server = redisClusterManager.searchServerKey(key)
        return CommandResponse(value = "${JumpConsistentHash.hash(key)}:${value}", server = server)
    }

    @PutMapping(
        "/set/{key}/{value}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun set(@PathVariable key: String, @PathVariable value: String): CommandResponse {
        val value = redisClusterManager.set(key, value)
        val server = redisClusterManager.searchServerKey(key)
        return CommandResponse(value = value, server = server)
    }

    @GetMapping(
        "/mget/{keys}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun mget(@PathVariable keys: Set<String>): Map<String, String?> {
        val value = redisClusterManager.mget(keys)
        return value
    }

    @PostMapping(
        "/mset",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun mset(@RequestBody body: Map<String, String>): Map<String, String> {
        val value = redisClusterManager.mset(body)
        return value
    }

    @GetMapping(
        "/hash/{key}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getKeyHash(@PathVariable key: String): ApiResponse {
        return ApiResponse("Get Key Hash", body = redisClusterManager.calculateHash(key))
    }
}
