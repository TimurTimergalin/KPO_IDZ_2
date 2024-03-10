package com.restaurant.server.routes

import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.dishesStatistics
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.statisticsRoutes() {
    routing {
        authenticate("auth-session-exists", "auth-session-admin") {
            webSocket("/ws/statistics") {
                RestaurantDao.dishesStatistics().let {
                    Json.encodeToString(it)
                }.let {
                    "{\"dishes\": $it}"
                }.let {
                    Frame.Text(it)
                }.let {
                    outgoing.send(it)
                }
            }

            get("/statistics") {
                call.respond(FreeMarkerContent("statistics.ftl", null))
            }
        }
    }
}
