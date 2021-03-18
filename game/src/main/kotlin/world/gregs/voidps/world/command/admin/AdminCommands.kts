import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.player.effect.Hidden
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.tele
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.obj.Stairs
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
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
    val id = content.toIntOrNull()
    val defs: NPCDefinitions = get()
    val spawns: NPCLoader = get()
    val npc = if (id != null) {
        spawns.spawn(id, player.tile, Direction.NORTH)
    } else {
        println(
                """
            - name: $content
              x: ${player.tile.x}
              y: ${player.tile.y}
              plane: ${player.tile.plane}
        """.trimIndent()
        )
        spawns.spawn(content, player.tile, Direction.NORTH)
    }
//    npc?.movement?.frozen = true
}

val botCounter = AtomicInteger(0)

val loginQueue: LoginQueue by inject()
val playerStorage: StorageStrategy<Player> by inject()

Command where { prefix == "save" } then {
    playerStorage.save(player.name, player)
}

val definitions: ItemDefinitions by inject()

Command where { prefix == "item" } then {
    val parts = content.split(" ")
    val id = parts[0].toIntOrNull() ?: definitions.getId(parts[0].toLowerCase())
    var amount = parts.getOrNull(1) ?: "1"
    if (amount == "max") {
        amount = Int.MAX_VALUE.toString()
    }
    player.inventory.add(id, amount.toInt())
    println(player.inventory.result)
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
    player.setVar("life_points", 990)
    for (skill in Skill.all) {
        player.experience.set(skill, 14000000.0)
    }
}

Command where { prefix == "hide" } then {
    player.effects.toggle(Hidden)
}

Command where { prefix == "pos" || prefix == "mypos" } then {
    player.message(player.tile.toString())
    println(player.tile)
}

Command where { prefix == "reload" } then {
    when(content) {
        "stairs" -> {
            get<Stairs>().load()
        }
    }
}