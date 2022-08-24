package com.example

import StatusChangesObj
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.net.Socket


class FileFunction {
    var socket: Socket

    constructor(socket: Socket) {
        this.socket = socket
    }

    fun getFileInfo(fileName: String): String {
        val file = File("files/$fileName")
        val fileSize = file.length()
        val obj = JSONObject()
        obj.apply {
            put("doesFileExist", file.exists())
            put("fileSize", fileSize)
            put("fileName", fileName)
        }
        return obj.toString()
    }

    fun sendFile(socket: Socket, filePath: String, fileOutputStream: DataOutputStream, bytesRemaining: Long = 0) {
        var bytes = 0
        var fileSize = 0
        val file = File("files/$filePath")
        println(file.exists())
        if (file.exists()) {
            val fileInputStream = RandomAccessFile(file, "rws")
            val scope = CoroutineScope(Dispatchers.IO)
            val buffer = ByteArray(4 * 1024)
            if (bytesRemaining > 0) {
                fileInputStream.seek(bytesRemaining)
            }
            scope.launch {
                while (fileInputStream.read(buffer).also { bytes = it } > 0) {
                    if (StatusChangesObj.status != DownloadState.DOWNLOADING) break
                    fileOutputStream.write(buffer, 0, bytes)
                    fileSize += bytes
                    println(fileSize)
                    fileOutputStream.flush()
                }
                fileInputStream.close()
                fileOutputStream.close()
                socket.close()
            }

        }


    }
}