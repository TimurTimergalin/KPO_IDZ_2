package com.restaurant.kitchen

import com.restaurant.kitchen.events.NewDishOrdered
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WorkSlots(private val maxSize: Int) {
    private val slots: MutableMap<Long, Channel<Boolean>> = mutableMapOf()
    private val lock = Mutex()

    suspend fun withSlotsAvailable(action: suspend (Int) -> NewDishOrdered?): Pair<NewDishOrdered, Channel<Boolean>>? {
        lock.withLock {
            if (slots.count() < maxSize) {
                return action(slots.count())?.let {
                    Pair(it, Channel<Boolean>(1).also { it1 -> slots[it.orderDishId] = it1 })
                }
            }
            return null
        }
    }

    suspend fun remove(id: Long) {
        lock.withLock {
            slots.remove(id) ?: throw RuntimeException("такого заказа нет")  // Никогда не должно случиться
        }
    }

    suspend fun send(id: Long, message: Boolean): Boolean {
        lock.withLock {
            slots[id].let {
                if (it == null) {
                    return false
                }
                it.send(message)
                return true
            }
        }
    }
}