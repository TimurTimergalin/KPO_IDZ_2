package com.restaurant.server.routes.order.events

import com.restaurant.events.Event

data class DishCountChanged(val dishId: Long, val count: Int) : Event