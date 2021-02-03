package com.indiemove.reduster.support

import io.lettuce.core.KeyValue

interface IClusterManager {
    fun mset(map: Map<String, String>): Map<String, String>
    fun mget(keys: Set<String>): List<KeyValue<String, String>>
    fun set(key: String, value: String): String?
    fun get(key: String): String?
    fun insert(host: String, port: Int)
    fun delete(host: String, port: Int)
}
