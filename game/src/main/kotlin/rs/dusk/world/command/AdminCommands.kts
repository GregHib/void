import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.character.player.PlayerRegistered
import rs.dusk.engine.model.entity.character.update.visual.player.tele
import rs.dusk.engine.model.map.area.area
import rs.dusk.utility.inject
import rs.dusk.world.command.Command
import rs.dusk.world.interact.npc.spawn.NPCSpawn
import rs.dusk.world.interact.player.spawn.login.Login
import rs.dusk.world.interact.player.spawn.login.LoginResponse
import java.util.concurrent.atomic.AtomicInteger

val bus: EventBus by inject()
val scheduler: Scheduler by inject()

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
    println("""
        - id: $id
          x: ${player.tile.x}
          y: ${player.tile.y}
          plane: ${player.tile.plane}
    """.trimIndent())
    val npc = bus.emit(NPCSpawn(id, player.tile, Direction.NORTH))
//    npc?.movement?.frozen = true
}

val botCounter = AtomicInteger(0)

Command where { prefix == "bot" } then {
    player.tile.area(4).forEach { tile ->
        val callback = { response: LoginResponse ->
            if (response is LoginResponse.Success) {
                val bot = response.player
                bus.emit(PlayerRegistered(bot))
                bus.emit(Registered(bot))
                bot.viewport.loaded = true
                scheduler.add {
                    delay(1)
                    bot.tele(tile.x, tile.y, tile.plane)
                }
            }
        }
        bus.emit(
            Login(
                "Bot ${botCounter.getAndIncrement()}",
                callback = callback
            )
        )
    }
}

Command where { prefix == "inter" } then {
    val id = content.toInt()
    if(id == -1) {
        val id = player.interfaces.get("main_screen") ?: return@then
        player.interfaces.close(id)
    } else {
        player.interfaces.open(id)
    }
}