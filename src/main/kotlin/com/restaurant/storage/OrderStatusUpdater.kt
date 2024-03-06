package com.restaurant.storage

import com.restaurant.events.EventHandler
import com.restaurant.events.subscribe
import com.restaurant.events.unsubscribe
import com.restaurant.kitchen.events.DishCancelled
import com.restaurant.kitchen.events.FinishedCooking
import com.restaurant.kitchen.events.StartedCooking
import com.restaurant.server.routes.order.events.OrderFinished
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.orm.OrderDish
import com.restaurant.storage.orm.Orders
import com.restaurant.storage.orm.OrdersToDishes
import java.time.LocalDateTime

class OrderStatusUpdater {
    private val orderFinishedHandler = object : EventHandler<OrderFinished> {
        override suspend fun handle(event: OrderFinished) {
            RestaurantDao.getById(event.orderId, Orders)?.apply {
                endTime = LocalDateTime.now()
                finishReason = event.status
                flushChanges()
            }
        }
    }

    private val cookingStartedHandler = object : EventHandler<StartedCooking> {
        override suspend fun handle(event: StartedCooking) {
            RestaurantDao.getById(event.orderDishId, OrdersToDishes)?.apply {
                status = OrderDish.COOKING
                cookingStartTime = LocalDateTime.now()
                flushChanges()
            }
        }
    }

    private val cancelledHandler = object : EventHandler<DishCancelled> {
        override suspend fun handle(event: DishCancelled) {
            RestaurantDao.getById(event.orderDishId, OrdersToDishes)?.apply {
                status = OrderDish.CANCELLED
                cookingFinishingTime = LocalDateTime.now()
                flushChanges()
            }
        }
    }

    private val cookingFinishedHandler = object : EventHandler<FinishedCooking> {
        override suspend fun handle(event: FinishedCooking) {
            RestaurantDao.getById(event.orderDishId, OrdersToDishes)?.apply {
                status = OrderDish.FINISHED
                cookingFinishingTime = LocalDateTime.now()
                flushChanges()
            }
        }
    }

    suspend fun subscribe() {
        orderFinishedHandler.subscribe()
        cookingStartedHandler.subscribe()
        cookingFinishedHandler.subscribe()
        cancelledHandler.subscribe()
    }

    suspend fun unsubscribe() {
        orderFinishedHandler.unsubscribe()
        cookingStartedHandler.unsubscribe()
        cookingFinishedHandler.unsubscribe()
        cancelledHandler.unsubscribe()
    }
}