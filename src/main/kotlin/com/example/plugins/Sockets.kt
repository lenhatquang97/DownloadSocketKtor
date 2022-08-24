package com.example.plugins

import StatusChangesObj
import com.example.DownloadState
import com.example.FileFunction
import com.example.SocketPort
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
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
    object Server {
        //This function will only be used for getting machine's ip, not on VM
        fun exposeIpAddress() {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("google.com", 80))
                println("Your address is ${socket.localAddress} and your port is $SocketPort")
            }
        }

        fun start() {
            CoroutineScope(Dispatchers.IO).launch {
                val serverSocket = ServerSocket(SocketPort)
                println("Socket Server listening at ${serverSocket.localSocketAddress}")
                println("-------------------------------------------------------------")
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
                            val jsonObj = JSONObject(result)
                            val command = jsonObj.getString("command")
                            val content = jsonObj.getString("content")
                            if (command != null) {
                                when (command) {
                                    "ping" -> println("Welcome to our server!!!")
                                    "getFileInfo" -> {
                                        val res = fileFunction.getFileInfo(content)
                                        write.write("$res\n".toByteArray())
                                    }
                                    "sendFile" -> {
                                        StatusChangesObj.status = DownloadState.DOWNLOADING
                                        fileFunction.sendFile(socket, content, write)
                                    }
                                    "pause" -> {
                                        StatusChangesObj.status = DownloadState.PAUSED
                                        continued = false
                                    }
                                    "resume" -> {
                                        val fileName = content.split("?")[0]
                                        val bytesRemaining = content.split("?")[1].toLong()
                                        StatusChangesObj.status = DownloadState.DOWNLOADING
                                        fileFunction.sendFile(socket, fileName, write, bytesRemaining)
                                    }
                                    "stop" -> {
                                        StatusChangesObj.status = DownloadState.STOPPED
                                        continued = false
                                    }
                                    "exit" -> {
                                        println("Client disconnected")
                                        continued = false
                                        socket.close()
                                    }
                                    "retry" -> {
                                        StatusChangesObj.status = DownloadState.DOWNLOADING
                                        fileFunction.sendFile(socket, content, write)
                                    }
                                    else -> {
                                        println("Invalid command")
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

