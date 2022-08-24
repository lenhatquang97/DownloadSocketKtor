import java.net.Socket

interface StatusChanged {
    fun onStatusChanged(socket: Socket)
    fun onPause(socket: Socket)
    fun onStop(socket: Socket)
    fun onResume(socket: Socket)
}

object StatusChangesObj{
    var status = "Downloading"
    val statusChangedEvent = object : StatusChanged {
        override fun onStatusChanged(socket: Socket) {
            when (status){
                "pause" -> onPause(socket)
                "stop" -> onStop(socket)
                "resume" -> onResume(socket)
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
