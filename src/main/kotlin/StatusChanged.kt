import com.example.DownloadState
import java.net.Socket

interface StatusChanged {
    fun onStatusChanged(socket: Socket)
    fun onPause(socket: Socket)
    fun onStop(socket: Socket)
    fun onResume(socket: Socket)
}

object StatusChangesObj {
    var status = DownloadState.DOWNLOADING
    val statusChangedEvent = object : StatusChanged {
        override fun onStatusChanged(socket: Socket) {
            when (status) {
                DownloadState.PAUSED -> onPause(socket)
                DownloadState.STOPPED -> onStop(socket)
                else -> {}
            }
        }

        override fun onPause(socket: Socket) {
            socket.close()
            //next step
        }

        override fun onStop(socket: Socket) {
            socket.close()
            //next step
        }

        override fun onResume(socket: Socket) {
            //next step
        }
    }
}
