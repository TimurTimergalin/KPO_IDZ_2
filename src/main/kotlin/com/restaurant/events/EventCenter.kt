package com.restaurant.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

class EventCenter private constructor(private val scope: CoroutineScope) {
    private val subscribers: MutableMap<KClass<out Event>, MutableList<EventHandler<out Event>>> = mutableMapOf()
    private val lock = Mutex()

    suspend fun <T : Event> subscribe(handler: EventHandler<T>, eventType: KClass<T>) {
        lock.withLock {
            subscribers.getOrPut(eventType) { mutableListOf() }.add(handler)
        }

    }

    suspend inline fun <reified T : Event> subscribe(handler: EventHandler<T>) {
        subscribe(handler, T::class)
    }

    suspend fun <T : Event> unsubscribe(handler: EventHandler<T>, eventType: KClass<T>): Boolean {
        lock.withLock {
            return subscribers.getOrDefault(eventType, mutableListOf()).remove(handler)
        }
    }

    suspend inline fun <reified T : Event> unsubscribe(handler: EventHandler<T>): Boolean {
        return unsubscribe(handler, T::class)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Event> notify(event: T, eventType: KClass<T>) {
        lock.withLock {
            subscribers.getOrDefault(eventType, mutableListOf()).forEach {
                (it as? EventHandler<T>)?.  // На самом деле этот каст всегда сработает
                apply { scope.launch(Dispatchers.Default) { handle(event) } }
            }
        }
    }

    suspend inline fun <reified T : Event> notify(event: T) {
        notify(event, T::class)
    }

    companion object {
        private var inst: EventCenter? = null

        fun get(): EventCenter {
            return inst!!
        }

        fun setScope(scope: CoroutineScope) {
            inst = EventCenter(scope)
        }
    }
}