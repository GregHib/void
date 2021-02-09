import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.effect.Colour
import world.gregs.voidps.engine.entity.character.effect.Transform
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()
val login: LoginQueue by inject()
val bus: EventBus by inject()

Command where { prefix == "kill" } then {
    players.indexed.forEachIndexed { index, bot ->
        if (bot != null && bot.name.startsWith("Bot")) {
            players.remove(bot.tile, bot)
            players.remove(bot.tile.chunk, bot)
        }
    }
    GlobalScope.launch {
        delay(600)
        players.indexed.forEachIndexed { index, bot ->
            if (bot != null && bot.name.startsWith("Bot")) {
                players.indexed[index] = null
            }
        }
    }
}

Command where { prefix == "players" } then {
    println("Players: ${players.indexed.filterNotNull().size}")
}

Command where { prefix == "under" } then {
    players[player.tile]?.filterNotNull()?.forEach {
        println("$it - ${player.viewport.players.current.contains(it)}")
    }
}

Command where { prefix == "anim" } then {
    player.setAnimation(content.toInt())// 863
}

Command where { prefix == "gfx" } then {
    val id = content.toInt()
    player.setGraphic(id)// 93
}

Command where { prefix == "tfm" || prefix == "transform" } then {
    val id = content.toInt()
    if (id != -1) {
        player.effects.add(Transform(id))
    } else {
        player.effects.remove("transform")
    }
}

Command where { prefix == "overlay" } then {
    player.effects.add(Colour(-2108002746, 10, 100))
}

Command where { prefix == "chat" } then {
    player.forceChat = "Testing"
}

Command where { prefix == "move" } then {
    player.setForceMovement(Tile(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

Command where { prefix == "hit" } then {
    player.addHit(Hit(10, Hit.Mark.Healed, 255))
}

Command where { prefix == "time" } then {
    player.setTimeBar(true, 0, 60, 1)
}

Command where { prefix == "watch" } then {
    val bot = players.indexed.firstOrNull { it != null && it.name.startsWith("Bot") }
    if (bot != null) {
        player.watch(bot)
    }
}

Command where { prefix == "shoot" } then {
    player.shoot(15, player.tile.addY(10))
}

Command where { prefix == "face" } then {
    val parts = content.split(" ")
    player.face(parts[0].toInt(), parts[1].toInt())
}

Command where { prefix == "chunk" } then {
    val chunks: DynamicChunks = get()
    chunks.set(player.tile.chunk, player.tile.chunk, rotation = 2)
}

Command where { prefix == "chunk2" } then {
    val chunks: DynamicChunks = get()
    chunks.remove(player.tile.chunk)
}