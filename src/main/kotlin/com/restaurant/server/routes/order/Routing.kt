package com.restaurant.server.routes.order

import com.restaurant.server.UserSession
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.orm.Users
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*


fun Application.orderRoutes() {
    routing {
        authenticate("auth-session-exists") {
            get("/my-order") {
                call.respond(FreeMarkerContent("my_order.ftl", null))
            }

            webSocket("/ws/my-order") {
                OrderSession.create(
                    this,
                    call.sessions.get<UserSession>()!!.id.let { RestaurantDao.getById(it, Users) }!!
                ).handle()
            }
        }
    }
}
