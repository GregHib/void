import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.effect.Colour
import world.gregs.voidps.engine.entity.character.effect.Transform
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()

on<Command>({ prefix == "kill" }) { player: Player ->
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

on<Command>({ prefix == "players" }) { player: Player ->
    println("Players: ${players.indexed.filterNotNull().size}")
}

on<Command>({ prefix == "under" }) { player: Player ->
    players[player.tile]?.filterNotNull()?.forEach {
        println("$it - ${player.viewport.players.current.contains(it)}")
    }
}

on<Command>({ prefix == "anim" }) { player: Player ->
    player.setAnimation(content.toInt())// 863
}

on<Command>({ prefix == "gfx" }) { player: Player ->
    val id = content.toInt()
    player.setGraphic(id)// 93
}

on<Command>({ prefix == "tfm" || prefix == "transform" }) { player: Player ->
    val id = content.toInt()
    if (id != -1) {
        player.effects.add(Transform(id))
    } else {
        player.effects.remove("transform")
    }
}

on<Command>({ prefix == "overlay" }) { player: Player ->
    player.effects.add(Colour(-2108002746, 10, 100))
}

on<Command>({ prefix == "chat" }) { player: Player ->
    player.forceChat = "Testing"
}

on<Command>({ prefix == "move" }) { player: Player ->
    player.setForceMovement(Tile(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

on<Command>({ prefix == "hit" }) { player: Player ->
    player.addHit(Hit(10, Hit.Mark.Healed, 255))
}

on<Command>({ prefix == "time" }) { player: Player ->
    player.setTimeBar(true, 0, 60, 1)
}

on<Command>({ prefix == "watch" }) { player: Player ->
    val bot = players.indexed.firstOrNull { it != null && it.name.startsWith("Bot") }
    if (bot != null) {
        player.watch(bot)
    }
}

on<Command>({ prefix == "shoot" }) { player: Player ->
    player.shoot(15, player.tile.addY(10))
}

on<Command>({ prefix == "face" }) { player: Player ->
    val parts = content.split(" ")
    player.face(parts[0].toInt(), parts[1].toInt())
}

on<Command>({ prefix == "chunk" }) { player: Player ->
    val chunks: DynamicChunks = get()
    chunks.set(player.tile.chunk, player.tile.chunk, rotation = 2)
}

on<Command>({ prefix == "chunk2" }) { player: Player ->
    val chunks: DynamicChunks = get()
    chunks.remove(player.tile.chunk)
}