import org.redrune.GameServer
import org.redrune.world.World
import kotlin.system.exitProcess

class Bootstrap {

}

fun main() {
    try {
        GameServer(World(1)).run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}