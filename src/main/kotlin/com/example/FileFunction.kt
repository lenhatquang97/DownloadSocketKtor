package com.example

import StatusChanged
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import status
import java.io.File
import java.io.FileInputStream


class FileFunction {
    var socket: Socket
    constructor(socket: Socket){
        this.socket = socket
    }
    fun getFileInfo(fileName: String): String {
        val file = File(fileName)
        val fileSize = file.length()
        return "{fileSize: $fileSize, fileName: $fileName}"
    }
    fun sendFile(socket: Socket, statusChangedEvent: StatusChanged) {
        val filePath = ""
        var bytes = 0
        val file = File(filePath)
        val fileInputStream = FileInputStream(file)
        val fileOutputStream = socket.openWriteChannel(autoFlush = true)
        val scope = CoroutineScope(Dispatchers.IO)
        val buffer = ByteArray(4 * 1024)
        scope.launch {
            fileOutputStream.writeLong(file.length())
        }
        while (fileInputStream.read(buffer).also { bytes = it } != -1) {
            statusChangedEvent.onStatusChanged(socket)
            if(status != "Downloading") break
            scope.launch {
                fileOutputStream.writeFully(buffer, 0, bytes)
                fileOutputStream.flush()
            }
        }
        fileInputStream.close()
    }
}