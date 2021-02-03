package com.indiemove.reduster.support

import com.google.common.collect.Ordering
import com.indiemove.reduster.domain.ValueComparableMap
import io.lettuce.core.api.StatefulRedisConnection
import java.util.Comparator
import java.util.concurrent.ConcurrentSkipListMap

abstract class AbstractClusterManager<T> : IClusterManager {
    var hashTables: ValueComparableMap<String, Int> = ValueComparableMap(Ordering.natural())
    var buckets: ConcurrentSkipListMap<Int, T> = ConcurrentSkipListMap(Comparator.naturalOrder())
    var weight: Int = 10
}
