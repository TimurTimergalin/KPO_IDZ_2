package com.restaurant.storage

import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.createAdmins
import io.ktor.server.application.*

fun Application.configureDatabaseConnection() {
    with(environment.config) {
        val dao = RestaurantDao
        println()
        dao.apply {
            init(
                driver = property("storage.connection.driver").getString(),
                url = property("storage.connection.url").getString(),
                username = property("storage.connection.user").getString(),
                password = property("storage.connection.password").getString()
            )
            createAdmins(property("storage.admins").getList())
        }
    }

}
