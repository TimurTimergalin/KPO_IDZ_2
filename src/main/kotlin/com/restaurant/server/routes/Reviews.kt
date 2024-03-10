package com.restaurant.server.routes

import com.restaurant.server.UserSession
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.getAvgRating
import com.restaurant.storage.dao.getOtherReviews
import com.restaurant.storage.dao.getUserReview
import com.restaurant.storage.orm.Dishes
import com.restaurant.storage.orm.Review
import com.restaurant.storage.orm.Users
import com.restaurant.storage.orm.unnamedSnapshot
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ktorm.entity.add

fun Application.reviewsRoutes() {
    routing {
        authenticate("auth-session-exists") {
            get("/reviews/{id}") {
                val dishId = call.parameters["id"]?.toLongOrNull()
                if (dishId == null) {
                    call.respond(HttpStatusCode.BadRequest, "wrong dish id")
                    return@get
                }

                val dish = RestaurantDao.getById(dishId, Dishes)
                if (dish == null) {
                    call.respond(HttpStatusCode.NotFound, "unknown dish")
                    return@get
                }

                val avg = RestaurantDao.getAvgRating(dish)
                call.respond(FreeMarkerContent("reviews.ftl", mapOf("name" to dish.name, "rating" to avg)))
            }

            webSocket("/ws/reviews/{id}") {
                val dishId = call.parameters["id"]?.toLongOrNull()
                if (dishId == null) {
                    call.respond(HttpStatusCode.BadRequest, "wrong dish id")
                    return@webSocket
                }

                val dish = RestaurantDao.getById(dishId, Dishes)
                if (dish == null) {
                    call.respond(HttpStatusCode.NotFound, "unknown dish")
                    return@webSocket
                }

                val user = RestaurantDao.getById(call.sessions.get<UserSession>()!!.id, Users)!!

                var userReview = RestaurantDao.getUserReview(dish, user)
                userReview?.unnamedSnapshot.let {
                    Json.encodeToString(it)
                }.let {
                    "{\"user-review\": $it}"
                }.let {
                    Frame.Text(it)
                }.let {
                    outgoing.send(it)
                }

                val reviews = RestaurantDao.getOtherReviews(dish, user)
                val total = reviews.count()
                val portionSize = 20
                var nextPortion = 0

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        if (text == "more") {
                            val sublist = try {
                                reviews.subList(nextPortion, nextPortion + portionSize)
                            } catch (e: IndexOutOfBoundsException) {
                                reviews.subList(nextPortion, total)
                            }

                            sublist.let {
                                Json.encodeToString(it)
                            }.let {
                                nextPortion += portionSize
                                "{\"reviews\": $it, \"more\": ${nextPortion < total}}"
                            }.let {
                                Frame.Text(it)
                            }.let {
                                outgoing.send(it)
                            }
                        } else {
                            try {
                                val review = Json.decodeFromString<Review.UnnamedSnapshot>(text)
                                Review {
                                    author = user
                                    this.dish = dish
                                    rating = review.rating
                                    this.text = review.text
                                }.let {
                                    if (userReview == null) {
                                        RestaurantDao.reviews.add(it)
                                        userReview = it
                                    } else {
                                        userReview!!.rating = it.rating
                                        userReview!!.text = it.text
                                        userReview!!.flushChanges()
                                    }
                                }

                            } catch (e: SerializationException) {
                                errorFrame("wrong message").let {
                                    outgoing.send(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
