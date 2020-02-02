import org.redrune.GameServer
import org.redrune.world.World
import kotlin.system.exitProcess

fun main() {
    try {
        GameServer(World(1)).run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}