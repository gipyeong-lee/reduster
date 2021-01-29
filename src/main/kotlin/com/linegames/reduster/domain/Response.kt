package com.linegames.reduster.domain

data class ApiResponse(val message: String, val body: Any?)
data class CommandResponse(val server: Int, val value: String?)
data class ServerInfoResponse(val uri: String, val port: Int, val hashKey: String)
data class Bucket(val hashKey: Int, val info: String)
data class BucketResponse(val buckets: List<Bucket>)

data class HashKey(val key : String, val hashKey: Int, val serverKey: Int)
data class HashKeyResponse(val keys: List<HashKey>)
