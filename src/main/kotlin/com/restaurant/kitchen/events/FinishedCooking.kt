package com.restaurant.kitchen.events

import com.restaurant.events.Event

// Событие завершения готовки блюда
data class FinishedCooking(val orderDishId: Long) : Event