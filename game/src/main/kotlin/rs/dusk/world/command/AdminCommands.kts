import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.entity.character.update.visual.player.tele
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.area.area
import rs.dusk.utility.inject
import rs.dusk.world.command.Command
import rs.dusk.world.interact.dialogue.destroy
import rs.dusk.world.interact.entity.npc.spawn.NPCSpawn
import rs.dusk.world.interact.entity.player.spawn.login.Login
import rs.dusk.world.interact.entity.player.spawn.login.LoginResponse
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

Command where { prefix == "item" } then {
    val parts = content.split(" ")
    val id = parts[0].toInt()
    val amount = if(parts.size > 1) parts[1].toInt() else 1
    player.inventory.add(id, amount)
}

Command where { prefix == "test" } then {
    player.dialogue {
//        player dialogue "Hi"
//        NPC(id = 1, tile = Tile.EMPTY) dialogue "Hello Adventurer"
//        player statement "Words"
//        println("Choice: ${player title "Should we change something?" choice """
//            Yes change something
//            No leave it how it is
//        """}")
        println("Destroy ${destroy("<br>Can't be undone.", 11694)}")
//        println("Integer: ${intEntry("Enter a number")}")
//        println("String: ${stringEntry("Enter some text")}")
//        levelUp("Congratzzzz", 12)
//        itemBox("""
//            The two halves of the skull fit perfectly, they appear to
//            have a fixing point, perhaps they are to be mounted on
//            something?
//        """, 9009, 650)
//        println("Make: ${makeAmount(listOf(385, 329), "Make sets", 23)}")
    }
}