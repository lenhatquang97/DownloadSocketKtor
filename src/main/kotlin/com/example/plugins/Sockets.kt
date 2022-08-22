package com.example.plugins

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import java.net.InetSocketAddress
import java.net.Socket

fun Application.configureSockets() {
    val selectorManager = SelectorManager(Dispatchers.IO)
    EchoApp.Server.exposeIpAddress()
    EchoApp.Server.start()
}
/**
 * Two mains are provided, you must first start EchoApp.Server, and then EchoApp.Client.
 * You can also start EchoApp.Server and then use a telnet client to connect to the echo server.
 */
object EchoApp {
    val selectorManager = ActorSelectorManager(Dispatchers.IO)
    val DefaultPort = (System.getenv("PORT") ?: "23567").toInt()

    object Server {
        fun exposeIpAddress(){
            Socket().use { socket ->
                socket.connect(InetSocketAddress("google.com", 80))
                println("Your address and port are ${socket.localAddress}")
            }
        }
        fun start() {
            runBlocking {
                val serverSocket = aSocket(selectorManager).tcp().bind(port = DefaultPort)
                println("Echo Server listening at ${serverSocket.localAddress}")
                while (true) {
                    val socket = serverSocket.accept()
                    println("Accepted ${socket.remoteAddress}")
                    launch {
                        val read = socket.openReadChannel()
                        val write = socket.openWriteChannel(autoFlush = true)
                        try {
                            while (true) {
                                val line = read.readUTF8Line()
                                write.writeStringUtf8("$line\n")
                            }
                        } catch (e: Throwable) {
                            socket.close()
                        }
                    }
                }
            }
        }
    }
}

