package com.restaurant.kitchen

import com.restaurant.events.EventCenter
import com.restaurant.events.EventHandler
import com.restaurant.events.subscribe
import com.restaurant.events.unsubscribe
import com.restaurant.kitchen.events.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean

class Kitchen(private val scope: CoroutineScope) {
    private val queue: EventQueue = EventQueue()
    private val slots = WorkSlots(COOKS_AVAILABLE)
    private val cancelledSet: CancelledSet = CancelledSet()

    private val cancellationHandler = object : EventHandler<DishCancelled> {
        override suspend fun handle(event: DishCancelled) {
            if (slots.send(event.orderDishId, COOKING_CANCELLED)) {
                queue.remove(event.orderDishId)
            } else {
                cancelledSet.add(event.orderDishId)
            }
        }
    }
    private val closingHandler = object : EventHandler<KitchenClosed> {
        override suspend fun handle(event: KitchenClosed) {
            distributeChannel.send(CLOSE_KITCHEN)
        }
    }
    private val newDishHandler = object : EventHandler<NewDishOrdered> {
        override suspend fun handle(event: NewDishOrdered) {
            cancelledSet.ifNotCancelled(event.orderDishId) {
                queue.add(event)
                distributeChannel.send(CHECK_QUEUE)
            }

            if (distributionStarted.compareAndSet(false, true)) {
                withContext(Dispatchers.Default) {
                    launch { distribute() }
                }
            }
        }
    }

    private var distributeChannel: Channel<Boolean> = Channel(Channel.UNLIMITED)
    private val distributionStarted = AtomicBoolean(false)

    suspend fun subscribe() {
        newDishHandler.subscribe()
        cancellationHandler.subscribe()
        closingHandler.subscribe()
    }

    suspend fun unsubscribe() {
        newDishHandler.unsubscribe()
        cancellationHandler.unsubscribe()
        closingHandler.unsubscribe()
    }

    // Корутина, распределяющая заказы по "поварам"
    // Приоритет заказов следующий:
    // - если больше половины поваров свободны, распределяет самые долгие заказы;
    // - иначе, распределяет самые быстрые заказы.
    // Таким образом, долгие заказы не будут оставляться на самый конец, но при этом и не остановят работу кухни
    private suspend fun distribute() = coroutineScope {
        println("Distributing")
        for (msg in distributeChannel) {
            if (msg == CLOSE_KITCHEN) {
                break
            }
            slots.withSlotsAvailable { s ->
                if (2 * s < COOKS_AVAILABLE) {
                    queue.pollFirst()
                } else {
                    queue.pollLast()
                }
            }?.apply {
                val (e, c) = this
                val finishingJob = launch(Dispatchers.Default) { finish(e.orderDishId, e.cookingTimeMillis) }
                launch(Dispatchers.Default) {
                    work(e.orderDishId, c, this, finishingJob)
                }
            }
        }
    }

    // Корутина, эмулирующая работу повара
    private suspend fun work(id: Long, chan: Channel<Boolean>, scope: CoroutineScope, finishingJob: Job) {
        EventCenter.get().notify(StartedCooking(id))
        for (mes in chan) {
            if (mes == COOKING_COMPLETE) {
                scope.launch { EventCenter.get().notify(FinishedCooking(id)) }
            } else {
                finishingJob.cancel()
            }
            slots.remove(id)
            distributeChannel.send(CHECK_QUEUE)
            break
        }
    }

    // Корутина, эмулирующая "таймер" повара - когда время закончится, блюдо приготовится
    private suspend fun finish(id: Long, duration: Long) {
        delay(duration)
        slots.send(id, COOKING_COMPLETE)
    }

    companion object {
        private const val COOKS_AVAILABLE = 6  // Сколько блюд может готовиться одновременно

        private const val COOKING_CANCELLED = false
        private const val COOKING_COMPLETE = true

        private const val CHECK_QUEUE = true
        private const val CLOSE_KITCHEN = false
    }
}