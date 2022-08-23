package com.example.plugins

import com.example.SocketPort
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

    object Server {
        //This function will only be used for getting machine's ip, not on VM
        fun exposeIpAddress(){
            Socket().use { socket ->
                socket.connect(InetSocketAddress("google.com", 80))
                println("Your address is ${socket.localAddress}")
            }
        }
        fun start() {
            runBlocking {
                val serverSocket = aSocket(selectorManager).tcp().bind(port = SocketPort)
                println("Socket Server listening at ${serverSocket.localAddress}")
                while (true) {
                    val socket = serverSocket.accept()
                    println("Accepted ${socket.remoteAddress}")
                    launch {
                        val read = socket.openReadChannel()
                        val write = socket.openWriteChannel(autoFlush = true)
                        var continued = true
                        try {
                            while (continued) {
                                val result = read.readUTF8Line()
                                if(result != null){
                                    with(result){
                                        when{
                                            contains("getFileInfo") ->{

                                            }
                                            contains("sendFile") -> {

                                            }
                                            equals("pause") -> {

                                            }
                                            equals("resume") -> {

                                            }
                                            equals("stop") -> {

                                            }
                                            equals("exit") -> {
                                                println("Client disconnected")
                                                socket.close()
                                                continued = false
                                            }
                                            else -> {
                                                println("Received: $result")
                                                write.writeStringUtf8("Received: $result")
                                            }

                                        }
                                    }
                                }

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

