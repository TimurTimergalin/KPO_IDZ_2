package com.restaurant.storage.orm

import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.countOrderDishes
import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.LocalDateTimeSerializer
import com.restaurant.storage.orm.misc.TableWithId
import kotlinx.serialization.Serializable
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.toList
import org.ktorm.schema.Table
import org.ktorm.schema.*
import java.time.LocalDateTime

interface Order : EntityWithId<Order> {
    companion object : Entity.Factory<Order>() {
        const val PAID = "paid"
        const val CANCELLED = "cancelled"
        const val REFUSED = "refused"
    }

    override val id: Long
    var author: User?
    var finishReason: String?
    var startTime: LocalDateTime
    var endTime: LocalDateTime?

    val orderDishes get() = RestaurantDao.orderDishes.filter { it.orderId eq id }

    @Serializable
    data class SummarySnapshot(
        val id: Long,
        val finishReason: String?,
        @Serializable(with = LocalDateTimeSerializer::class) val endTime: LocalDateTime?,
        val dishes: List<OrderDish.NameSnapshot>
    )
}

val Order.summarySnapshot
    get() = Order.SummarySnapshot(
        id,
        finishReason,
        endTime,
        RestaurantDao.countOrderDishes(this).map { OrderDish.NameSnapshot(it.first, it.second) })

open class Orders(alias: String?) : TableWithId<Order>("orders", alias) {
    companion object : Orders(null)

    override fun aliased(alias: String): Table<Order> {
        return Orders(alias)
    }

    override val id = long("id").primaryKey().bindTo { it.id }
    val authorId = long("author_id").references(Users) { it.author }
    val finishReason = varchar("finish_reason").bindTo { it.finishReason }
    val startTime = datetime("start_time").bindTo { it.startTime }
    val endTime = datetime("end_time").bindTo { it.endTime }

    val author get() = authorId.referenceTable as Users
}
