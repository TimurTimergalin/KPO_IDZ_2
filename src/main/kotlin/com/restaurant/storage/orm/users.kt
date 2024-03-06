package com.restaurant.storage.orm

import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.TableWithId
import kotlinx.serialization.Serializable
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface User: EntityWithId<User> {
    companion object : Entity.Factory<User>()

    override val id: Long
    var login: String
    var password: String
    var role: String

    @Serializable
    data class Snapshot(val id: Long, val login: String)
}

val User.snapshot get() = User.Snapshot(id, login)

open class Users(alias: String?): TableWithId<User>("users", alias) {
    companion object : Users(null)

    override fun aliased(alias: String): Table<User> {
        return Users(alias)
    }

    override val id = long("id").primaryKey().bindTo { it.id }
    val login = varchar("login").bindTo { it.login }
    val password = varchar("hashed_pass").bindTo { it.password }
    val role = varchar("role").bindTo { it.role }
}
