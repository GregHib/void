import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.effect.colourOverlay
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()

on<Command>({ prefix == "kill" }) { _: Player ->
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

on<Command>({ prefix == "players" }) { _: Player ->
    println("Players: ${players.indexed.filterNotNull().size}")
}

on<Command>({ prefix == "under" }) { player: Player ->
    players[player.tile]?.forEach {
        println("$it - ${player.viewport.players.current.contains(it)}")
    }
}

on<Command>({ prefix == "anim" }) { player: Player ->
    when (content) {
        "-1", "" -> player.clearAnimation()
        else -> player.setAnimation(content, override = true)// 863
    }
}

on<Command>({ prefix == "gfx" }) { player: Player ->
    when (content) {
        "-1", "" -> player.clearGraphic()
        else -> player.setGraphic(content)// 93
    }
}

on<Command>({ prefix == "proj" }) { player: Player ->
    player.shoot(content, player.tile.add(0, 5), delay = 0, flightTime = 400)
}

on<Command>({ prefix == "tfm" || prefix == "transform" }) { player: Player ->
    player.transform(content)
}

on<Command>({ prefix == "overlay" }) { player: Player ->
    player.colourOverlay(-2108002746, 10, 100)
}

on<Command>({ prefix == "chat" }) { player: Player ->
    player.forceChat = content
}

on<Command>({ prefix == "move" }) { player: Player ->
    player.setForceMovement(Delta(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

on<Command>({ prefix == "hit" }) { player: Player ->
    player.addHit(Hit(10, Hit.Mark.Regular, 255))
}

on<Command>({ prefix == "time" }) { player: Player ->
    player.setTimeBar(true, 0, 60, 1)
}

on<Command>({ prefix == "watch" }) { player: Player ->
    val bot = players.get(content)
    if (bot != null) {
        player.watch(bot)
    }
}

on<Command>({ prefix == "shoot" }) { player: Player ->
    player.shoot("15", player.tile.addY(10))
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

on<Command>({ prefix == "skill" }) { player: Player ->
    player.skillLevel = content.toInt()
}

on<Command>({ prefix == "cmb" }) { player: Player ->
    player.combatLevel = content.toInt()
}

on<Command>({ prefix == "tgl" }) { player: Player ->
    player.toggleSkillLevel()
}

on<Command>({ prefix == "sum" }) { player: Player ->
    player.summoningCombatLevel = content.toInt()
}