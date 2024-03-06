package com.restaurant.storage.dao

import com.restaurant.storage.orm.*
import com.restaurant.storage.orm.misc.EntityWithId
import com.restaurant.storage.orm.misc.TableWithId
import com.restaurant.storage.orm.operators.CustomSqlFormatter
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.expression.SqlFormatter
import org.ktorm.schema.Table
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.postgresql.util.PSQLException

object RestaurantDao {
    private var _database: Database? = null
    val database: Database get() = _database!!  // База данных должна быть инициализирвана для корректно работы,
    // поэтому постоянные null-проверки излишни

    fun init(
        driver: String,
        url: String,
        username: String,
        password: String
    ) {
        HikariDataSource(HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            maximumPoolSize = 1
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            setUsername(username)
            setPassword(password)
            validate()
        }).also {
            _database = Database.connect(it, dialect = object : PostgreSqlDialect() {
                override fun createSqlFormatter(
                    database: Database,
                    beautifySql: Boolean,
                    indentSize: Int
                ): SqlFormatter {
                    return CustomSqlFormatter(database, beautifySql, indentSize)
                }
            })
        }
    }

    val dishes: EntitySequence<Dish, TableWithId<Dish>> get() = database.sequenceOf(Dishes)
    val orders get() = database.sequenceOf(Orders)
    val orderDishes get() = database.sequenceOf(OrdersToDishes)
    val reviews get() = database.sequenceOf(Reviews)
    val users get() = database.sequenceOf(Users)

    fun <T : EntityWithId<T>> getById(id: Long, table: TableWithId<T>): T? {
        return database.sequenceOf(table).find { it.id eq id }
    }

    fun <T: Entity<T>> add(entity: T, table: Table<T>): Boolean {
        return try {
            database.sequenceOf(table).add(entity)
            true
        } catch (e: PSQLException) {
            false
        }
    }
}

fun <T: EntityWithId<T>> EntitySequence<T, TableWithId<T>>.ids() = map { it.id }
