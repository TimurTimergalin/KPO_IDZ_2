package com.restaurant.server

import com.restaurant.storage.dao.RestaurantDao
import com.restaurant.storage.dao.validateCredentials
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*

data class UserSession(val id: Long, val username: String, val role: String): Principal

fun Application.configureSecurity() {
    val secretEncryptionKey = hex(environment.config.property("session.keys.secretEncryptionKey").getString())
    val secretSignKey = hex(environment.config.property("session.keys.secretSignKey").getString())
    install(Sessions) {
        cookie<UserSession>("user-session") {
            cookie.extensions["SameSite"] = "lax"
            cookie.path = "/"
            cookie.maxAgeInSeconds = 600
            transform(SessionTransportTransformerEncrypt(secretEncryptionKey, secretSignKey))
        }
    }
    install(Authentication) {
        form("auth-form") {
            userParamName = "login"
            passwordParamName = "password"

            validate { credentials ->
                val user = RestaurantDao.validateCredentials(credentials.name, credentials.password)
                if (user != null) {
                    UserSession(user.id, user.login, user.role)
                } else {
                    null
                }
            }
            challenge("/sign-in/Invalid credentials")
        }

        session<UserSession>("auth-session-exists") {
            validate { it }
            challenge("/sign-in")
        }

        session<UserSession>("auth-session-admin") {
            validate { session ->
                if (session.role == "admin") {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "You do not have access to this resource")
            }
        }
    }
}
