package com.restaurant.storage.orm

import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.TableWithId
import kotlinx.serialization.Serializable
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDateTime


interface OrderDish: EntityWithId<OrderDish> {
    companion object : Entity.Factory<OrderDish>() {
        const val COOKING = "cooking"
        const val ORDERED = "ordered"
        const val FINISHED = "finished"
        const val CANCELLED = "cancelled"
    }

    override val id: Long
    var order: Order
    var dish: Dish
    var orderTime: LocalDateTime
    var cookingStartTime: LocalDateTime?
    var cookingFinishingTime: LocalDateTime?
    var status: String

    @Serializable
    data class NameSnapshot(val name: String, val count: Int)

    @Serializable
    data class StatusSnapshot(val id: Long, val name: String, val status: String)
}

val OrderDish.statusSnapshot get() = OrderDish.StatusSnapshot(id, dish.name, status)


open class OrdersToDishes(alias: String?): TableWithId<OrderDish>("orders_to_dishes", alias) {
    companion object : OrdersToDishes(null)

    override fun aliased(alias: String): Table<OrderDish> {
        return OrdersToDishes(alias)
    }

    override val id = long("id").primaryKey().bindTo { it.id }
    val orderId = long("order_id").references(Orders) { it.order }
    val dishId = long("dish_id").references(Dishes) { it.dish }
    val orderTime = datetime("order_time").bindTo { it.orderTime }
    val cookingStartTime = datetime("cooking_start_time").bindTo { it.cookingStartTime }
    val cookingFinishingTime = datetime("cooking_finishing_time").bindTo { it.cookingFinishingTime }
    val status = varchar("status").bindTo { it.status }

    val order get(): Orders {
        val table = orderId.referenceTable
        return table as Orders
    }
    val dish get() = dishId.referenceTable as Dishes
}
