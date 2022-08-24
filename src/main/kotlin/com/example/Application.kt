package com.example

import com.example.plugins.configureSockets
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = BackendPort) {
        CoroutineScope(Dispatchers.IO).launch {
            configureSockets()
        }
        //disable this so that we only use Socket for server demo
        //configureRouting()
    }.start(wait = true)
}
