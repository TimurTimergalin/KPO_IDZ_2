package com.restaurant.server.routes.order.json

import kotlinx.serialization.Serializable

@Serializable
data class Paid(val paid: Boolean)
