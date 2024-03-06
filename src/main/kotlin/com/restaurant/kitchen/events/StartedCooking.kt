package com.restaurant.kitchen.events

import com.restaurant.events.Event

// Событие начала готовки блюда
data class StartedCooking(val orderDishId: Long) : Event