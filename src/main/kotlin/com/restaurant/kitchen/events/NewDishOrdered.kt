package com.restaurant.kitchen.events

import com.restaurant.events.Event

// Событие заказа нового блюда
data class NewDishOrdered(val orderDishId: Long, val cookingTimeMillis: Long) : Event