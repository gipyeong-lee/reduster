package com.linegames.reduster.controller

import com.linegames.reduster.domain.ApiResponse
import com.linegames.reduster.domain.CommandResponse
import com.linegames.reduster.support.RedisClusterManager
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/redis/cmd")
class RedisCommandController(val redisClusterManager: RedisClusterManager) {
    @GetMapping(
        "/get/{key}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun get(@PathVariable key: String): CommandResponse {
        val value = redisClusterManager.getValue(key)
        val server = redisClusterManager.searchServerKey(key)
        return CommandResponse(value = value, server = server)
    }

    @PutMapping(
        "/set/{key}/{value}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun set(@PathVariable key: String, @PathVariable value: String): CommandResponse {
        val value = redisClusterManager.setValue(key, value)
        val server = redisClusterManager.searchServerKey(key)
        return CommandResponse(value = value, server = server)
    }

    @GetMapping(
        "/hash/{key}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getKeyHash(@PathVariable key: String): ApiResponse {
        return ApiResponse("Get Key Hash", body = redisClusterManager.calculateHash(key))
    }
}
