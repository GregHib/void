import org.redrune.GameServer

fun main() {
    try {
        GameServer.run()
    } catch (e: Exception) {
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}