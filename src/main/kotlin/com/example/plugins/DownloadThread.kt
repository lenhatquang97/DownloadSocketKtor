package com.example.plugins

import com.example.DownloadState
import com.example.FileFunction
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class DownloadThread: Thread {
    lateinit var socket: Socket
    constructor(socket: Socket) : super() {
        this.socket = socket
    }
    override fun run() {
        super.run()
        val read = socket.getInputStream()
        val write = socket.getOutputStream()
        val bufferReader = BufferedReader(InputStreamReader(read))
        val fileFunction = FileFunction(socket)
        while(true){
            try{
                val result = bufferReader.readLine()
                println("Waiting")
                if(result == null || result.contains("exit")){
                    socket.close()
                    println("Socket closed")
                    return
                } else {
                    val jsonObj = JSONObject(result)
                    val command = jsonObj.getString("command")
                    val content = jsonObj.getString("content")
                    println(jsonObj)
                    if (command != null) {
                        when (command) {
                            "ping" -> println("Welcome to our server!!!")
                            "getFileInfo" -> {
                                val res = fileFunction.getFileInfo(content)
                                write.write("$res\n".toByteArray())
                                write.flush()
                            }
                            "sendFile" -> {
                                val onHandle: () -> Unit = {
                                    read.close()
                                    write.close()
                                }
                                StatusChangesObj.status = DownloadState.DOWNLOADING
                                fileFunction.sendFile(socket, content, write, onHandle)
                            }
                            "pause" -> {
                                StatusChangesObj.status = DownloadState.PAUSED
                            }
                            "resume" -> {
                                val fileName = content.split("?")[0]
                                val bytesRemaining = content.split("?")[1].toLong()
                                StatusChangesObj.status = DownloadState.DOWNLOADING
                                val onHandle: () -> Unit = {
                                    read.close()
                                    write.close()
                                }
                                fileFunction.sendFile(socket, fileName, write, onHandle, bytesRemaining)
                            }
                            "stop" -> {
                                StatusChangesObj.status = DownloadState.STOPPED
                            }
                            "exit" -> {
                                println("Client disconnected")
                                socket.close()
                                read.close()
                                write.close()
                            }
                            "retry" -> {
                                val onHandle: () -> Unit = {
                                    read.close()
                                    write.close()
                                }
                                StatusChangesObj.status = DownloadState.DOWNLOADING
                                fileFunction.sendFile(socket, content, write, onHandle)
                            }
                            else -> {
                                println("Invalid command")
                            }
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                return
            }
        }
    }
}