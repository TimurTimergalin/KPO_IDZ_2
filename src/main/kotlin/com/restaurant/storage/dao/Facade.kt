package com.restaurant.storage.dao

import com.restaurant.server.routes.order.json.ClientOrderItem
import com.restaurant.storage.orm.*
import com.restaurant.storage.orm.operators.extract
import com.restaurant.storage.orm.operators.now
import org.ktorm.database.Transaction
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.LocalDateTime

fun RestaurantDao.getUserByLogin(login: String): User? {
    return users.find { it.login eq login }
}

fun RestaurantDao.createAdmins(credentials: Iterable<String>) {
    for (cred in credentials) {
        val (login, password) = cred.split(":").let { Pair(it[0], it[1]) }

        registerUser(login, password, "admin")
    }
}

fun RestaurantDao.registerUser(login: String, password: String, role: String = "client"): Boolean {
    return User {
        this.login = login
        this.password = Passwords.generatePassword(password)
        this.role = role
    }.let {
        add(it, Users)
    }
}

fun RestaurantDao.validateCredentials(login: String, password: String): User? {
    with(getUserByLogin(login)) {
        if (this == null) {
            return null
        }
        return if (Passwords.checkPassword(this.password, password)) {
            this
        } else {
            null
        }
    }
}

fun RestaurantDao.hasOrder(user: User): Boolean {
    return orders.find {
        (it.authorId eq user.id) and (it.finishReason.isNull())
    } != null
}

fun RestaurantDao.countOrderDishes(order: Order): List<Tuple2<String, Int>> {
    return database
        .from(OrdersToDishes)
        .innerJoin(OrdersToDishes.dish, OrdersToDishes.dish.id eq order.id)
        .select(OrdersToDishes.dish.name, count(OrdersToDishes.dishId))
        .groupBy(OrdersToDishes.dishId, OrdersToDishes.dish.name)
        .map { tupleOf(it.getString(1)!!, it.getInt(2)) }
}

fun RestaurantDao.countDishesPopularityThisMonth(): Map<Long, Int> {
    return database
        .from(OrdersToDishes)
        .select(OrdersToDishes.dishId, count(OrdersToDishes.dishId))
        .where(
            (OrdersToDishes.status eq "finished") and (extract(
                "month",
                OrdersToDishes.cookingFinishingTime
            ) eq extract("month", now()))
        )
        .groupBy(OrdersToDishes.dishId).map { it.getLong(1) to it.getInt(2) }.toMap()
}

fun RestaurantDao.calculateAvgRating(): Map<Long, Double> {
    return database
        .from(Reviews)
        .rightJoin(Reviews.dish, Reviews.dishId eq Reviews.dish.id)
        .select(Reviews.dish.id, avg(Reviews.rating))
        .groupBy(Reviews.dish.id).map { it.getLong(1) to it.getDouble(2) }.toMap()
}

fun RestaurantDao.calculateDishRevenueThisMonth(): Map<Long, Double> {
    return database
        .from(OrdersToDishes)
        .rightJoin(OrdersToDishes.dish, OrdersToDishes.dishId eq OrdersToDishes.dish.id)
        .select(OrdersToDishes.dish.id, sum(OrdersToDishes.dish.price))
        .where(
            (OrdersToDishes.status eq "finished") and (extract(
                "month",
                OrdersToDishes.cookingFinishingTime
            ) eq extract("month", now()))
        )
        .groupBy(OrdersToDishes.dish.id).map { it.getLong(1) to it.getDouble(2) }.toMap()
}

fun RestaurantDao.dishesStatistics() = dishes.map {
    val s1 = countDishesPopularityThisMonth()
    val s2 = calculateAvgRating()
    val s3 = calculateDishRevenueThisMonth()
    Dish.StatisticsSnapshot(it.id , it.name, s1[it.id] ?: 0, s2[it.id] ?: 0.0, s3[it.id] ?: 0.0)
}

fun RestaurantDao.getUserReview(dish: Dish, user: User): Review? {
    return reviews.find { (it.dishId eq dish.id) and (it.authorId eq user.id) }
}

fun RestaurantDao.getOtherReviews(dish: Dish, user: User): List<Review.Snapshot> {
    return reviews.filter { (it.dishId eq dish.id) and (it.authorId neq user.id) }.map { it.snapshot }
}

fun RestaurantDao.getAvgRating(dish: Dish): Double {
    return reviews.filter { it.dishId eq dish.id }.averageBy { it.rating } ?: 0.0
}

fun RestaurantDao.validateOrder(items: List<ClientOrderItem>): Boolean {
    if (items.isEmpty()) {
        return false
    }
    val counts = dishes.map { it.id to it.count }.toMap()
    items.forEach {
        if (counts[it.dishId] == null || counts[it.dishId]!! < it.count) {
            return false
        }
    }
    return true
}

fun RestaurantDao.createOrder(items: List<ClientOrderItem>, author: User): Pair<Order, List<OrderDish>>  {
    return Order {
        this.author = author
        finishReason = null
        startTime = LocalDateTime.now()
        endTime = null
    }.let { order ->
        database.useTransaction {
            orders.add(order)
            order to updateOrder(items, order, it)
        }
    }
}

fun RestaurantDao.updateOrder(items: List<ClientOrderItem>, order: Order): List<OrderDish> {
    return database.useTransaction {
        updateOrder(items, order, it)
    }
}

fun RestaurantDao.updateOrder(items: List<ClientOrderItem>, order: Order, transaction: Transaction): List<OrderDish> {
    return transaction.run {
        val res = mutableListOf<OrderDish>()
        items.forEach {item ->
            val dish = getById(item.dishId, Dishes)!!
            for (i in 1..item.count) {
                OrderDish {
                    this.order = order
                    this.dish = dish
                    orderTime = LocalDateTime.now()
                    cookingStartTime = null
                    cookingFinishingTime = null
                    status = "ordered"
                }.let {
                    orderDishes.add(it)
                    res.add(it)
                    --dish.count
                }
            }
            dish.flushChanges()
        }
        res
    }
}