import org.redrune.GameServer
import kotlin.system.exitProcess

fun main() {
    try {
        GameServer.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}