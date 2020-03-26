import org.redrune.GameServer
import org.redrune.world.World
import kotlin.system.exitProcess

fun main() {
    try {
        val world = World(1)

        GameServer(world).run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}