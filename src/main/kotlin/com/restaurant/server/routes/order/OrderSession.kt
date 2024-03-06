package com.restaurant.server.routes.order

import com.restaurant.TimeConverter
import com.restaurant.events.EventCenter
import com.restaurant.events.EventHandler
import com.restaurant.events.subscribe
import com.restaurant.events.unsubscribe
import com.restaurant.kitchen.events.DishCancelled
import com.restaurant.kitchen.events.FinishedCooking
import com.restaurant.kitchen.events.NewDishOrdered
import com.restaurant.kitchen.events.StartedCooking
import com.restaurant.server.routes.errorFrame
import com.restaurant.server.routes.order.events.DishCountChanged
import com.restaurant.server.routes.order.events.OrderFinished
import com.restaurant.server.routes.order.json.*
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.createOrder
import com.restaurant.storage.dao.updateOrder
import com.restaurant.storage.dao.validateOrder
import com.restaurant.storage.orm.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.isNull
import org.ktorm.entity.find
import org.ktorm.entity.forEach
import org.ktorm.entity.map
import java.sql.SQLException

typealias WsAction = suspend DefaultWebSocketServerSession.() -> Unit

inline fun <reified T> Json.decodeFromStringOrNull(st: String): T? {
    return try {
        decodeFromString<T>(st)
    } catch (e: SerializationException) {
        null
    }
}

suspend fun <T> T?.tryOn(action: suspend (T) -> Unit): Boolean? {
    return this?.let {
        action(it)
        true
    }
}


object SessionMessages {
    fun dishCountMessage(dishId: Long, count: Int) = mapOf(
        "dish-count" to mapOf(
            "id" to JsonPrimitive(dishId),
            "count" to JsonPrimitive(count)
        ).let { JsonObject(it) }
    ).let {
        JsonObject(it).toString()
    }

    fun orderUpdatesUnavailable() = mapOf(
        "updates-unavailable" to JsonPrimitive(true)
    ).let {
        JsonObject(it).toString()
    }

    fun orderFinished() = mapOf(
        "order-finished" to JsonPrimitive(true)
    ).let {
        JsonObject(it).toString()
    }

    fun statusMessage(orderDishId: Long, status: String) = mapOf(
        "status-update" to mapOf(
            "id" to JsonPrimitive(orderDishId),
            "status" to JsonPrimitive(status)
        ).let {
            JsonObject(it)
        }
    ).let {
        JsonObject(it).toString()
    }
}


class OrderSession(val sessionContext: DefaultWebSocketServerSession, val user: User) {
    private val lock = Mutex()
    private var order: Order? = null
    private val orderedDishes = mutableSetOf<Long>()
    private val finishedDishes = mutableSetOf<Long>()
    private var acceptUpdates = true

    private val onFinish = mutableSetOf<suspend () -> Unit>()


    private suspend fun init() {  // Конструкторы не могут быть suspend
        object : EventHandler<DishCountChanged> {
            override suspend fun handle(event: DishCountChanged) {
                SessionMessages.dishCountMessage(event.dishId, event.count).let {
                    Frame.Text(it)
                }.let {
                    sessionContext.outgoing.send(it)
                }
            }
        }.apply {
            subscribe()
            onFinish.add { unsubscribe() }
        }

        object : EventHandler<StartedCooking> {
            override suspend fun handle(event: StartedCooking) {
                lock.withLock {
                    if (!orderedDishes.contains(event.orderDishId)) {
                        return@withLock
                    }
                    if (acceptUpdates) {
                        acceptUpdates = false
                        SessionMessages.orderUpdatesUnavailable().let {
                            Frame.Text(it)
                        }.let {
                            sessionContext.outgoing.send(it)
                        }
                    }

                    SessionMessages.statusMessage(event.orderDishId, OrderDish.COOKING).let {
                        Frame.Text(it)
                    }.let {
                        sessionContext.outgoing.send(it)
                    }
                }
            }
        }.apply {
            subscribe()
            onFinish.add { unsubscribe() }
        }

        object : EventHandler<FinishedCooking> {
            override suspend fun handle(event: FinishedCooking) {
                lock.withLock {
                    if (!orderedDishes.contains(event.orderDishId)) {
                        return@withLock
                    }

                    orderedDishes.remove(event.orderDishId)
                    finishedDishes.add(event.orderDishId)

                    SessionMessages.statusMessage(event.orderDishId, OrderDish.FINISHED).let {
                        Frame.Text(it)
                    }.let {
                        sessionContext.outgoing.send(it)
                    }

                    if (orderedDishes.isEmpty()) {
                        SessionMessages.orderFinished().let {
                            Frame.Text(it)
                        }.let {
                            sessionContext.outgoing.send(it)
                        }
                    }
                }
            }
        }.apply {
            subscribe()
            onFinish.add { unsubscribe() }
        }
    }

    val addOrderDishes: suspend DefaultWebSocketServerSession.(List<OrderDish>) -> Unit = { orderDishes ->
        val ec = EventCenter.get()
        orderDishes.forEach {
            orderedDishes.add(it.id)
            ec.notify(NewDishOrdered(it.id, TimeConverter.get().minToMs(it.dish.cookingTime)))
        }
        orderDishes.map { it.dish }.toSet().forEach {
            ec.notify(DishCountChanged(it.id, it.count))
        }
        orderDishes.map { it.statusSnapshot }.let {
            Json.encodeToString(it)
        }.let {
            "{\"order-dishes\": $it}"
        }.let {
            Frame.Text(it)
        }.let {
            outgoing.send(it)
        }
    }

    private val newClientOrder: suspend DefaultWebSocketServerSession.(NewClientOrder) -> Unit = {
        lock.withLock {
            if (!RestaurantDao.validateOrder(it.newOrder)) {
                outgoing.send(errorFrame("Invalid order"))
            } else {
                try {
                    RestaurantDao.createOrder(it.newOrder, user)
                } catch (e: Exception) {
                    when (e) {
                        is SQLException, is NullPointerException -> {
                            outgoing.send(errorFrame("Invalid order"))
                        }

                        else -> throw e
                    }
                    null
                }?.let { (order, orderDishes) ->
                    this@OrderSession.order = order
                    addOrderDishes(orderDishes)
                }
            }
        }
    }

    private val cancel: suspend DefaultWebSocketServerSession.(Cancel) -> Unit = {
        lock.withLock {
            if (orderedDishes.isEmpty()) {
                outgoing.send(errorFrame("The order is done - impossible to cancel"))
            }
            val id = it.cancel.id

            if (!orderedDishes.remove(id) && !finishedDishes.remove(id)) {
                outgoing.send(errorFrame("Invalid order item"))
            } else {
                EventCenter.get().notify(DishCancelled(id))

                if (orderedDishes.isEmpty()) {
                    if (finishedDishes.isEmpty()) {
                        EventCenter.get().notify(OrderFinished(order!!.id, Order.CANCELLED))
                        order = null
                    } else {
                        SessionMessages.orderFinished().let {
                            Frame.Text(it)
                        }.let {
                            outgoing.send(it)
                        }
                    }
                }
            }
        }
    }

    private val updateClientOrder: suspend DefaultWebSocketServerSession.(UpdateClientOrder) -> Unit = {
        lock.withLock {
            if (!RestaurantDao.validateOrder(it.update)) {
                outgoing.send(errorFrame("Invalid order"))
                return@withLock
            }

            if (order == null) {
                outgoing.send(errorFrame("Invalid message"))
                return@withLock
            }

            if (!acceptUpdates) {
                outgoing.send(errorFrame("Cooking already started - impossible to add new dishes"))
                return@withLock
            }

            try {
                RestaurantDao.updateOrder(it.update, order!!)
            } catch (e: Exception) {
                when (e) {
                    is SQLException, is NullPointerException -> {
                        outgoing.send(errorFrame("Invalid order"))
                    }

                    else -> throw e
                }
                null
            }?.let { orderDishes ->
                addOrderDishes(orderDishes)
            }
        }
    }

    private val cancelAll: WsAction = {
        lock.withLock {
            if (order == null) {
                outgoing.send(errorFrame("Invalid message"))
                return@withLock
            }

            orderedDishes.forEach {
                EventCenter.get().notify(DishCancelled(it))
            }

            finishedDishes.forEach {
                EventCenter.get().notify(DishCancelled(it))
            }

            if (orderedDishes.isEmpty()) {
                Order.REFUSED
            } else {
                Order.CANCELLED
            }.let {
                EventCenter.get().notify(OrderFinished(order!!.id, it))
            }

            orderedDishes.clear()
            finishedDishes.clear()
            order = null
        }
    }

    private suspend fun payTimeout() {

    }

    private val paid: WsAction = {
        lock.withLock {
            if (order == null) {
                outgoing.send(errorFrame("Invalid message"))
                return@withLock
            }
            if (orderedDishes.isNotEmpty()) {
                outgoing.send(errorFrame("Invalid message"))
                return@withLock
            }

            EventCenter.get().notify(OrderFinished(order!!.id, Order.PAID))
            order = null
            finishedDishes.clear()
        }
    }

    private val initSession: WsAction = {
        RestaurantDao.dishes.map { it.propertySnapshot }.let {
            Json.encodeToString(it)
        }.let {
            "{\"dishes\": $it}"
        }.let {
            Frame.Text(it)
        }.let {
            outgoing.send(it)
        }

        RestaurantDao.orders.find { (it.authorId eq user.id) and it.finishReason.isNull() }?.let { order1 ->
            lock.withLock {
                order = order1

                order1.orderDishes.forEach {
                    if (it.status == OrderDish.ORDERED || it.status == OrderDish.COOKING) {
                        orderedDishes.add(it.id)
                    } else {
                        finishedDishes.add(it.id)
                    }

                    if (it.status == OrderDish.COOKING) {
                        acceptUpdates = false
                    }
                }

                if (finishedDishes.isNotEmpty()) {
                    acceptUpdates = false
                }

                order1.orderDishes.map { it.statusSnapshot }.let {
                    Json.encodeToString(it)
                }.let {
                    "{\"order-items\": $it, \"accept-updates\": $acceptUpdates, \"order-finished\": ${orderedDishes.isEmpty()}}"
                }.let {
                    Frame.Text(it)
                }.let {
                    outgoing.send(it)
                }
            }
        }
    }

    private val sessionAction: WsAction = {
        initSession()

        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                val valid = Json.decodeFromStringOrNull<NewClientOrder>(text).tryOn {
                    newClientOrder(it)
                } ?: Json.decodeFromStringOrNull<Cancel>(text).tryOn {
                    cancel(it)
                } ?: Json.decodeFromStringOrNull<UpdateClientOrder>(text).tryOn {
                    updateClientOrder(it)
                } ?: Json.decodeFromStringOrNull<CancelAll>(text).tryOn {
                    cancelAll()
                } ?: Json.decodeFromStringOrNull<Paid>(text).tryOn {
                    paid()
                } ?: false

                if (!valid) {
                    outgoing.send(errorFrame("Invalid message"))
                }
            }
        }

        println()
    }

    suspend fun handle() {
        sessionContext.apply {
            sessionAction()
            onFinish.forEach { it() }
        }
    }

    companion object {
        suspend fun create(sessionContext: DefaultWebSocketServerSession, user: User): OrderSession {
            return OrderSession(sessionContext, user).apply { init() }
        }
    }
}