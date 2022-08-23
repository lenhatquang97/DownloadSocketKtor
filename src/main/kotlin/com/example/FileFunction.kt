package com.example

import StatusChanged
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import status
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.Socket


class FileFunction {
    var socket: Socket
    constructor(socket: Socket){
        this.socket = socket
    }
    fun getFileInfo(fileName: String): String {
        val file = File("files/$fileName")
        val fileSize = file.length()
        return "{fileSize: $fileSize, fileName: $fileName}"
    }
    fun sendFile(socket: Socket, statusChangedEvent: StatusChanged, filePath: String, fileOutputStream: DataOutputStream) {
        var bytes = 0
        var fileSize = 0
        val file = File("files/$filePath")
        println(file.exists())
        if(file.exists()){
            val fileInputStream = FileInputStream(file)
            val scope = CoroutineScope(Dispatchers.IO)
            val buffer = ByteArray(4 * 1024)
            scope.launch {
                while (fileInputStream.read(buffer).also { bytes = it } > 0) {
                    statusChangedEvent.onStatusChanged(socket)
                    if(status != "Downloading") break
                    fileOutputStream.write(buffer, 0, bytes)
                    fileSize += bytes
                    println(fileSize)
                    fileOutputStream.flush()
                }
                fileInputStream.close()
                fileOutputStream.close()
            }

        }



    }
}