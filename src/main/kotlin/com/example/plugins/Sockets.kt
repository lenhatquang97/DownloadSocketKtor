package com.example.plugins

import com.example.FileFunction
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
import kotlinx.coroutines.CoroutineScope
import statusChangedEvent
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
                        val fileFunction = FileFunction(socket)
                        var continued = true
                        try {
                            while (continued) {
                                val result = read.readUTF8Line()
                                if(result != null){
                                    with(result){
                                        when{
                                            contains("Hello") -> {
                                                println("Welcome to our server!!!")
                                            }
                                            contains("getFileInfo") -> {
                                                val fileName = substringAfter("getFileInfo ")
                                                val result = fileFunction.getFileInfo(fileName)
                                                write.writeStringUtf8(result)
                                            }
                                            equals("pause") -> {
                                                statusChangedEvent.onPause(socket)
                                                continued = false
                                            }
                                            equals("resume") -> {
                                                statusChangedEvent.onResume(socket)
                                            }
                                            equals("stop") -> {
                                                statusChangedEvent.onStop(socket)
                                                continued = false
                                            }
                                            equals("exit") -> {
                                                println("Client disconnected")
                                                socket.close()
                                                continued = false
                                            }
                                            else -> {
                                                fileFunction.sendFile(socket, statusChangedEvent)
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

