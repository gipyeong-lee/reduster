package com.linegames.reduster.controller

import com.linegames.reduster.domain.ApiResponse
import com.linegames.reduster.domain.InsertServerRequest
import com.linegames.reduster.domain.ServerInfoResponse
import com.linegames.reduster.support.RedisClusterManager
import com.linegames.reduster.util.JumpConsistentHash
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
        val result = redisClusterManager.insert(body.host, body.port)
        return ApiResponse("$result Server", body = body)
    }

    @DeleteMapping(
        "/api/v1/redis/server",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun removeServer(@RequestBody body: InsertServerRequest): ApiResponse {
        val result = redisClusterManager.delete(body.host, body.port)
        return ApiResponse("$result Server", body = body)
    }

    @PostMapping(
        "/api/v1/redis/server/info",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun serverInfo(@RequestBody body: InsertServerRequest): ServerInfoResponse {
        val serverKey = JumpConsistentHash.hash(redisClusterManager.makeUri(body.host, body.port))
        val hashKey = serverKey.toString()
        return ServerInfoResponse(uri = body.host, port = body.port, hashKey)
    }
}
