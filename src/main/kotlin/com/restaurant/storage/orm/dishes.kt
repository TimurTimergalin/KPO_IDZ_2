package com.restaurant.storage.orm

import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.TableWithId
import kotlinx.serialization.Serializable
import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface Dish : EntityWithId<Dish> {
    companion object : Entity.Factory<Dish>()

    override val id: Long
    var name: String
    var count: Int
    var cookingTime: Int
    var price: Float


    @Serializable
    data class PropertySnapshot(val id: Long, val name: String, val count: Int, val cookingTime: Int, val price: Float)

    @Serializable
    data class StatisticsSnapshot(
        val id: Long,
        val name: String,
        val ordersCount: Int,
        val averageRating: Double,
        val revenue: Double
    )
}

val Dish.propertySnapshot get() = Dish.PropertySnapshot(id, name, count, cookingTime, price)

open class Dishes(alias: String?) : TableWithId<Dish>("dishes", alias) {
    companion object : Dishes(null)

    override fun aliased(alias: String): Table<Dish> {
        return Dishes(alias)
    }

    override val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val count = int("count").bindTo { it.count }
    val cookingTime = int("cooking_time_min").bindTo { it.cookingTime }
    val price = float("price").bindTo { it.price }
}
