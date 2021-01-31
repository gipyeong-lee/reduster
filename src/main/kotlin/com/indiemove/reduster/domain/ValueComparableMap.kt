package com.indiemove.reduster.domain

import com.google.common.base.Functions
import com.google.common.collect.Ordering
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

class ValueComparableMap<K : Comparable<K>?, V> private constructor(
    partialValueOrdering: Ordering<in V>,
    valueMap: HashMap<K, V?>
) :
    ConcurrentSkipListMap<K, V?>(
        partialValueOrdering //Apply the value ordering
            .onResultOf(Functions.forMap(valueMap)) //On the result of getting the value for the key from the map
            .compound(Ordering.natural())
    ) {
    //A map for doing lookups on the keys for comparison so we don't get infinite loops
    private val valueMap: MutableMap<K, V?>

    internal constructor(partialValueOrdering: Ordering<in V>) : this(partialValueOrdering, HashMap<K, V?>()) {}

    override fun put(key: K, value: V?): V? {
        if (valueMap.containsKey(key)) {
            //remove the key in the sorted set before adding the key again
            remove(key)
        }
        valueMap[key] = value //To get "real" unsorted values for the comparator
        return super.put(key, value) //Put it in value order
    }

    companion object {
        private const val serialVersionUID = 5424540773170446486L
    }

    init {
        this.valueMap = valueMap
    }
}
