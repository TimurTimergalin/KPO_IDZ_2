package com.restaurant.server.routes.order.events

import com.restaurant.events.Event

data class OrderFinished(val orderId: Long, val status: String): Event
