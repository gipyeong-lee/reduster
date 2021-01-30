package com.linegames.reduster.controller

import com.linegames.reduster.domain.*
import com.linegames.reduster.support.RedisClusterManager
import com.linegames.reduster.util.JumpConsistentHash
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
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

    @GetMapping(
        "/api/v1/redis/server/info/all",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun allServerInfo(): BucketResponse {
        val result = mutableListOf<Bucket>()
        val regex = "tcp_port:[0-9]+".toRegex(RegexOption.MULTILINE)
        redisClusterManager.buckets.entries.forEach { bucket ->
            result.add(Bucket(bucket.key,
                regex.find(bucket.value.sync().info("server"))?.value ?: ""))
        }
        return BucketResponse(result)
    }

    @GetMapping(
        "/api/v1/redis/keys/info/all",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun allKeysInfo(): HashKeyResponse {
        val result = mutableListOf<HashKey>()
        redisClusterManager.hashTables.entries.forEach { hash ->

            result.add(HashKey(hash.key, hash.value!!, redisClusterManager.searchServerKeyByHash(hash.value!!)))
        }
        return HashKeyResponse(result)
    }
}
