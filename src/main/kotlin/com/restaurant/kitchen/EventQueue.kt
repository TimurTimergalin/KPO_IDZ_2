package com.restaurant.kitchen

import com.restaurant.kitchen.events.NewDishOrdered
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class EventQueue {
    private val set = TreeSet<NewDishOrdered> { o1, o2 ->
        if (o1.cookingTimeMillis < o2.cookingTimeMillis) {
            -1
        } else if (o1.cookingTimeMillis == o2.cookingTimeMillis) {
            (o1.orderDishId - o2.orderDishId).toInt()
        } else
            1
    }

    private val lock = Mutex()

    suspend fun add(event: NewDishOrdered): Boolean {
        lock.withLock {
            return set.add(event)
        }
    }

    suspend fun remove(id: Long) {
        lock.withLock {
            set.removeIf { it.orderDishId == id }
        }
    }

    suspend fun pollFirst(): NewDishOrdered? {
        lock.withLock {
            return set.pollFirst()
        }
    }

    suspend fun pollLast(): NewDishOrdered? {
        lock.withLock {
            return set.pollLast()
        }
    }
}