package com.restaurant.server.routes

import com.restaurant.server.UserSession
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.orm.summarySnapshot
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.map

fun Application.clientOrdersRoutes() {
    routing {
        authenticate("auth-session-exists") {
            webSocket("/ws/my-orders") {
                val id = call.sessions.get<UserSession>()!!.id
                val orders = RestaurantDao.orders.filter { it.authorId eq id }.map { it.summarySnapshot }

                val total = orders.count()

                val portionSize = 20
                var nextPortion = 0

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        if (text == "more") {
                            val sublist = try {
                                orders.subList(nextPortion, nextPortion + portionSize)
                            } catch (e: IndexOutOfBoundsException) {
                                orders.subList(nextPortion, total)
                            }

                            outgoing.send(
                                Frame.Text(
                                    Json.encodeToString(sublist).let {
                                        "{\"orders\": $it, \"more\": ${nextPortion + portionSize < total}}"
                                    }
                                )
                            )
                            nextPortion += portionSize
                        }
                    } else {
                        errorFrame("wrong message").let {
                            outgoing.send(it)
                        }
                    }
                }
            }

            get("/my-orders") {
                call.respond(FreeMarkerContent("my_orders.ftl", null))
            }
        }
    }
}