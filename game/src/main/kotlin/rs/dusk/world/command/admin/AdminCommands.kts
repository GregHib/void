import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.entity.character.player.chat.ChatType
import rs.dusk.engine.entity.character.player.effect.Hidden
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.update.visual.player.tele
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.area.area
import rs.dusk.network.codec.game.encode.message
import rs.dusk.utility.get
import rs.dusk.utility.inject
import rs.dusk.world.command.Command
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
    println(
        """
        - id: $id
          x: ${player.tile.x}
          y: ${player.tile.y}
          plane: ${player.tile.plane}
    """.trimIndent()
    )
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

val definitions: ItemDefinitions by inject()

Command where { prefix == "item" } then {
    val parts = content.split(" ")
    val id = parts[0].toIntOrNull() ?: definitions.getId(parts[0].toLowerCase())
    var amount = parts.getOrNull(1) ?: "1"
    if(amount == "max") {
        amount = Int.MAX_VALUE.toString()
    }
    player.inventory.add(id, amount.toInt())
}

Command where { prefix == "find" } then {
    val items: ItemDefinitions = get()
    val search = content.toLowerCase()
    var found = false
    repeat(items.size) { id ->
        val def = items.getOrNull(id) ?: return@repeat
        if (def.name.toLowerCase().contains(search)) {
            player.message("[${def.name.toLowerCase()}] - id: $id", ChatType.Console)
            found = true
        }
    }
    if (!found) {
        player.message("No results found for '$search'", ChatType.Console)
    }
}

Command where { prefix == "clear" } then {
    player.inventory.clearAll()
}

Command where { prefix == "master" } then {
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
    }
}

Command where { prefix == "hide" } then {
    player.effects.toggle(Hidden)
}