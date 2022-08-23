package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = BackendPort){
        CoroutineScope(Dispatchers.IO).launch {
            configureSockets()
        }

        //disable this so that we only use Socket for server demo
        //configureRouting()
    }.start(wait = true)
}
