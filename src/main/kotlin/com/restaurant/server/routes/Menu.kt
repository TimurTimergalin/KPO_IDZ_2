package com.restaurant.server.routes

import com.restaurant.events.EventCenter
import com.restaurant.server.routes.order.events.DishCountChanged
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.orm.Dish
import com.restaurant.storage.orm.Dishes
import com.restaurant.storage.orm.propertySnapshot
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ktorm.entity.map

@Serializable
data class ChangeMenuEntry(val id: Long, val name: String, val count: Int, val cookingTime: Int, val price: Float)

fun errorFrame(message: String) = Frame.Text("{\"error\": \"$message\"}")

fun Application.menuRoutes() {
    routing {
        authenticate("auth-session-exists", "auth-session-admin") {
            get("/menu") {
                call.respond(FreeMarkerContent("menu.ftl", null))
            }

            post("/new-menu-item") {
                val form = call.receiveParameters()

                try {
                    Dish {
                        name = form["name"]!!
                        count = form["count"]?.toInt()!!
                        price = form["price"]?.toFloat()!!
                        cookingTime = form["cooking_time"]?.toInt()!!
                    }.let {
                        RestaurantDao.add(it, Dishes)
                    }
                    call.respondRedirect("/menu")
                } catch (e: NullPointerException) {
                    call.respond(HttpStatusCode.BadRequest, "invalid dish info")
                } catch (e: Exception) {  // Это на случай, если база данных отвалилась
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            post("/delete-menu-item") {
                val form = call.receiveParameters()

                try {
                    val id = form["id"]?.toLong()!!
                    with(RestaurantDao.getById(id, Dishes)) {
                        if (this == null) {
                            call.respond(HttpStatusCode.NotFound, "No such menu item")
                        } else {
                            delete()
                            call.respondRedirect("/menu")
                        }
                    }
                } catch (e: NullPointerException) {
                    call.respond(HttpStatusCode.BadRequest, "invalid dish info")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message.toString())
                }
            }

            webSocket("/ws/menu") {
                outgoing.send(
                    Json.encodeToString(
                        mapOf("dishes" to RestaurantDao.dishes.map { Json.encodeToString(it.propertySnapshot) })
                    ).let { Frame.Text(it) }
                )

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val obj = Json.decodeFromString<ChangeMenuEntry>(text)
                            val dish = RestaurantDao.getById(obj.id, Dishes)

                            if (dish == null) {
                                outgoing.send(errorFrame("Unknown dish"))
                            } else {
                                dish.count = obj.count
                                dish.price = obj.price
                                dish.name = obj.name
                                dish.cookingTime = obj.cookingTime
                                dish.flushChanges()
                            }
                            EventCenter.get().notify(DishCountChanged(obj.id, obj.count))
                        } catch (e: SerializationException) {
                            outgoing.send(errorFrame("Invalid message"))
                        } catch (e: Exception) {
                            outgoing.send(errorFrame(e.message.toString()))
                        }
                    }
                }
            }
        }
    }
}