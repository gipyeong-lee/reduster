package com.indiemove.reduster.util

import org.springframework.stereotype.Component

@Component
class JumpConsistentHash {
    companion object {
        private val JUMP = 1L shl 31
        private val CONSTANT = 2862933555777941757UL
        private const val BUCKETS = 1024
        fun hash(key: String, buckets: Int? = BUCKETS): Int {
            return jumpConsistentHashByCode(key.hashCode(), buckets!!)
        }

        private fun jumpConsistentHashByCode(key: Int, buckets: Int): Int {
            checkBuckets(buckets)
            var k = key.toULong()
            var b = -1L
            var j = 0L
            while (j < buckets) {
                b = j
                k = k * CONSTANT + 1UL
                k shr 33
                j = ((b + 1) * (JUMP / ((k shr 33) + 1UL).toDouble())).toLong()
            }
            return b.toInt()
        }

        private fun checkBuckets(buckets: Int) {
            require(buckets >= 0) { "Buckets cannot be less than 0" }
        }
    }
}
