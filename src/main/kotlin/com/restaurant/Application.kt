package com.restaurant

import com.restaurant.events.EventCenter
import com.restaurant.kitchen.Kitchen
import com.restaurant.server.configureSecurity
import com.restaurant.server.configureSockets
import com.restaurant.server.configureTemplating
import com.restaurant.server.routes.configureRouting
import com.restaurant.storage.OrderStatusUpdater
import com.restaurant.storage.configureDatabaseConnection
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    EventCenter.setScope(this)
    Kitchen(this).subscribe()
    OrderStatusUpdater().subscribe()
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabaseConnection()
    configureSockets()
    configureTemplating()
    configureSecurity()
    configureRouting()

    TimeConverter.init(environment.config.property("time.multiplier").getString().toLong())
}
