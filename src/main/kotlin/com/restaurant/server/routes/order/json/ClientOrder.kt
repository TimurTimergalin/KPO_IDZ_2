package com.restaurant.server.routes.order.json

import kotlinx.serialization.Serializable

@Serializable
data class ClientOrderItem(val dishId: Long, val count: Int)


typealias ClientOrder = List<ClientOrderItem>

@Serializable
data class NewClientOrder(val newOrder: ClientOrder)

@Serializable
data class UpdateClientOrder(val update: ClientOrder)
