import kotlinx.coroutines.runBlocking
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.factory.NPCFactory
import rs.dusk.engine.model.entity.factory.PlayerFactory
import rs.dusk.engine.model.entity.index.player.command.Command
import rs.dusk.engine.model.entity.index.update.visual.player.tele
import rs.dusk.utility.inject
import java.util.concurrent.atomic.AtomicInteger

val playerFactory: PlayerFactory by inject()
val bus: EventBus by inject()
val npcFactory: NPCFactory by inject()

Command where { prefix == "tele" || prefix == "tp" } then {
    if (content.contains(",")) {
        val params = content.split(",")
        val plane = params[0].toInt()
        val x = params[1].toInt() shl 6 or params[3].toInt()
        val y = params[2].toInt() shl 6 or params[4].toInt()
        player.tele(x, y, plane)
    } else {
        val parts = content.split(" ")
        player.tele(parts[0].toInt(), parts[1].toInt(), if (parts.size > 2) parts[2].toInt() else 0)
    }
}

Command where { prefix == "npc" } then {
    val id = content.toIntOrNull() ?: 1
    npcFactory.spawn(id, player.tile.x, player.tile.y, player.tile.plane, Direction.NORTH)
}

val botCounter = AtomicInteger(0)

Command where { prefix == "bot" } then {
    runBlocking {
        val bot = playerFactory.spawn("Bot ${botCounter.getAndIncrement()}", player.tile).await()!!
        bus.emit(Registered(bot))
    }
}