package com.restaurant.server.routes.order.json

import kotlinx.serialization.Serializable

@Serializable
data class CancelIdHolder(val id: Long)

@Serializable
data class Cancel(val cancel: CancelIdHolder)

@Serializable
data class CancelAll(val cancelAll: Boolean)
