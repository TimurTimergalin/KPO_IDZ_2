package com.restaurant.storage.orm

import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.TableWithId
import kotlinx.serialization.Serializable
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.text

interface Review : EntityWithId<Review> {
    companion object : Entity.Factory<Review>()

    override val id: Long
    var author: User?
    var dish: Dish
    var rating: Int
    var text: String

    @Serializable
    data class Snapshot(val authorName: String?, val rating: Int, val text: String)

    @Serializable
    data class UnnamedSnapshot(val rating: Int, val text: String)
}

val Review.snapshot get() = Review.Snapshot(author?.login, rating, text)
val Review.unnamedSnapshot get() = Review.UnnamedSnapshot(rating, text)

open class Reviews(alias: String?) : TableWithId<Review>("reviews", alias) {
    companion object : Reviews(null)

    override fun aliased(alias: String): Table<Review> {
        return Reviews(alias)
    }

    override val id = long("id").primaryKey().bindTo { it.id }
    val authorId = long("author_id").references(Users) { it.author }
    val dishId = long("dish_id").references(Dishes) { it.dish }
    val rating = int("rating").bindTo { it.rating }
    val text = text("text").bindTo { it.text }

    val author get() = authorId.referenceTable as Users
    val dish get() = dishId.referenceTable as Dishes
}
