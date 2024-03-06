package com.restaurant.kitchen.events

import com.restaurant.events.Event


// Событие отмены блюда
data class DishCancelled(val orderDishId: Long) : Event