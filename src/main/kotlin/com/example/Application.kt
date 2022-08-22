package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*

fun main() {
    val port = System.getenv("PORT") ?: "23567"
    embeddedServer(Netty, port = port.toInt()) {
        configureSockets()
        configureRouting()
    }.start(wait = true)
}
