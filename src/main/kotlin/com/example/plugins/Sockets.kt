package com.example.plugins

import com.example.FileFunction
import com.example.SocketPort
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import statusChangedEvent
import java.net.InetSocketAddress
import java.net.Socket

fun Application.configureSockets() {
    DownloadUtility.Server.exposeIpAddress()
    DownloadUtility.Server.start()
}
object DownloadUtility {
    val selectorManager = ActorSelectorManager(Dispatchers.IO)

    object Server {
        //This function will only be used for getting machine's ip, not on VM
        fun exposeIpAddress() {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("google.com", 80))
                println("Your address is ${socket.localAddress} and your port is ${socket.localPort}")
            }
        }

        fun start() {
            CoroutineScope(Dispatchers.IO).launch {
                val serverSocket = aSocket(selectorManager).tcp().bind(port = SocketPort)
                println("Socket Server listening at ${serverSocket.localAddress}")
                println("-------------------------------------------------------")
                while (true) {
                    val socket = serverSocket.accept()
                    println("Accepted client: ${socket.remoteAddress}")
                    val read = socket.openReadChannel()
                    val write = socket.openWriteChannel(autoFlush = true)
                    val fileFunction = FileFunction(socket)
                    var continued = true
                    try {
                        while (continued) {
                            val result = read.readUTF8Line()
                            if (result != null) {
                                with(result) {
                                    when {
                                        contains("Hello") -> println("Welcome to our server!!!")
                                        contains("getFileInfo") -> {
                                            val fileName = substringAfter("getFileInfo ")
                                            val res = fileFunction.getFileInfo(fileName)
                                            write.writeStringUtf8("$res\n")
                                        }
                                        contains("sendFile") -> {
                                            val fileName = substringAfter("sendFile ")
                                            fileFunction.sendFile(socket, statusChangedEvent,fileName,write)
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
                                            println("Invalid command")
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

