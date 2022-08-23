package com.example.plugins

import com.example.FileFunction
import com.example.SocketPort
import io.ktor.network.selector.*
import io.ktor.server.application.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import statusChangedEvent
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
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
                val serverSocket = ServerSocket(SocketPort)
                println("Socket Server listening at ${serverSocket.localSocketAddress}")
                println("-------------------------------------------------------")
                while (true) {
                    val socket = serverSocket.accept()
                    println("Accepted client: ${socket.remoteSocketAddress}")
                    val read = DataInputStream(socket.getInputStream())
                    val write = DataOutputStream(socket.getOutputStream())
                    val fileFunction = FileFunction(socket)
                    var continued = true
                    try {
                        while (continued) {
                            val result = read.readLine()
                            if (result != null) {
                                println(result)
                                with(result) {
                                    when {
                                        contains("Hello") -> println("Welcome to our server!!!")
                                        contains("getFileInfo") -> {
                                            val fileName = substringAfter("getFileInfo ")
                                            val res = fileFunction.getFileInfo(fileName)
                                            write.write("$res\n".toByteArray())
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

