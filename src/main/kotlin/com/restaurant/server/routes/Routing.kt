package com.restaurant.server.routes

import com.restaurant.server.UserSession
import com.restaurant.server.routes.order.orderRoutes
import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.hasOrder
import com.restaurant.storage.dao.registerUser
import com.restaurant.storage.orm.Users
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureAuthRoutes() {
    routing {
        staticResources("/res", "internal")

        route("/sign-up/{err?}") {
            get {
                if (call.sessions.get<UserSession>() != null) {
                    call.respondRedirect("/")
                    return@get
                }
                call.respond(FreeMarkerContent("sign_up.ftl", mapOf("error_message" to (call.parameters["err"] ?: ""))))
            }
            post {
                val form = call.receiveParameters()
                if (!RestaurantDao.registerUser(form["login"]!!, form["password"]!!)) {
                    call.respondRedirect("/sign-up/Account with such login already exists")
                }
                call.respondRedirect("/sign-in")
            }
        }

        route("/sign-in/{err?}") {
            get {
                if (call.sessions.get<UserSession>() != null) {
                    call.respondRedirect("/")
                    return@get
                }
                call.respond(FreeMarkerContent("sign_in.ftl", mapOf("error_message" to (call.parameters["err"] ?: ""))))
            }
            authenticate("auth-form")
            {
                post {
                    call.sessions.set(call.principal<UserSession>())
                    call.respondRedirect("/")
                }
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/sign-in")
        }
    }
}

fun Application.configureMainMenuRoutes() {
    routing {
        authenticate("auth-session-exists") {
            get("/") {
                val session = call.sessions.get<UserSession>()!!  // Проверка auth-session-exists гарантирует не null
                val args = mapOf<String, Any>(
                    "hasOrder" to RestaurantDao.run {
                        hasOrder(getById(session.id, Users)!!)  // Пользователь точно существует
                                                    },
                    "username" to session.username
                )
                if (session.role == "admin") {
                    call.respond(
                        FreeMarkerContent(
                            "index_admin.ftl",
                            args
                        )
                    )
                } else {
                    call.respond(
                        FreeMarkerContent(
                            "index_client.ftl",
                            args
                        )
                    )
                }
                call.respondText("Welcome!")
            }

            get("/account") {
                val session = call.sessions.get<UserSession>()!!  // Проверка auth-session-exists гарантирует не null
                call.respond(FreeMarkerContent("account.ftl", mapOf("username" to session.username)))
            }
        }
    }
    menuRoutes()
    clientOrdersRoutes()
    statisticsRoutes()
    reviewsRoutes()
    orderRoutes()
}

fun Application.configureRouting() {
    install(AutoHeadResponse)
    configureAuthRoutes()
    configureMainMenuRoutes()
}
