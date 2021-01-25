package com.linegames.reduster.controller

import com.linegames.reduster.domain.ApiResponse
import com.linegames.reduster.domain.InsertServerRequest
import com.linegames.reduster.support.RedisClusterManager
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RedisManagerController(val redisClusterManager: RedisClusterManager) {
    @PostMapping(
        "/api/v1/redis/server",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun insertServer(@RequestBody body: InsertServerRequest): ApiResponse {
        val result = redisClusterManager.insert(body.host,body.port)
        return ApiResponse("$result Server", body = body)
    }

    @DeleteMapping(
        "/api/v1/redis/server",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun removeServer(@RequestBody body: InsertServerRequest): ApiResponse {
        val result = redisClusterManager.delete(body.host,body.port)
        return ApiResponse("$result Server", body = body)
    }

}
