package com.restaurant.events

interface EventHandler<T: Event> {
    suspend fun handle(event: T)
}

suspend inline fun <reified T: Event> EventHandler<T>.subscribe() {
    EventCenter.get().subscribe(this)
}

suspend inline fun <reified T: Event> EventHandler<T>.unsubscribe() {
    EventCenter.get().unsubscribe(this)
}
