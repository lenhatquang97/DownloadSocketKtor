package com.example.plugins

import StatusChangesObj
import com.example.DownloadState
import com.example.FileFunction
import com.example.SocketPort
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
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
            val serverSocket = ServerSocket(SocketPort)
            while (true) {
                val socket = serverSocket.accept()
                println("${socket.remoteSocketAddress} connected")
                DownloadThread(socket).start()
            }

        }
    }
}

