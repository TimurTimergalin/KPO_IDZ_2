package com.restaurant.kitchen

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CancelledSet {
    private val lock = Mutex()
    private val set = mutableSetOf<Long>()

    suspend fun add(id: Long) {
        lock.withLock {
            set.add(id)
        }
    }

    suspend fun ifNotCancelled(id: Long, action: suspend () -> Unit) {
        val cond: Boolean
        lock.withLock {
            cond = !set.remove(id)
        }
        if (cond) {
            action()
        }
    }
}