package world.gregs.voidps.world.command.debug

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.command
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()

command({ prefix == "kill" }) { _: Player ->
    players.forEach { bot ->
        if (bot.name.startsWith("Bot")) {
            players.remove(bot)
        }
    }
    GlobalScope.launch {
        delay(600)
        players.forEach { bot ->
            if (bot.name.startsWith("Bot")) {
                players.remove(bot)
            }
        }
    }
}

command({ prefix == "players" }) { player: Player ->
    player.message("Players: ${players.size}, ${player.viewport?.players?.localCount}")
}

command({ prefix == "under" }) { player: Player ->
    players[player.tile].forEach {
        println("$it - ${players[it.tile]}")
    }
}

command({ prefix == "anim" }) { player: Player ->
    when (content) {
        "-1", "" -> player.clearAnimation()
        else -> player.setAnimation(content, override = true)// 863
    }
}

command({ prefix == "gfx" }) { player: Player ->
    when (content) {
        "-1", "" -> player.clearGraphic()
        else -> player.setGraphic(content)// 93
    }
}

command({ prefix == "proj" }) { player: Player ->
    player.shoot(content, player.tile.add(0, 5), delay = 0, flightTime = 400)
}

command({ prefix == "tfm" || prefix == "transform" }) { player: Player ->
    player.transform(content)
}

command({ prefix == "overlay" }) { player: Player ->
    player.colourOverlay(-2108002746, 10, 100)
}

command({ prefix == "chat" }) { player: Player ->
    player.forceChat = content
}

command({ prefix == "move" }) { player: Player ->
    player.setForceMovement(Delta(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

command({ prefix == "hit" }) { player: Player ->
    player.damage(content.toIntOrNull() ?: 10)
}

command({ prefix == "time" }) { player: Player ->
    player.setTimeBar(true, 0, 60, 1)
}

command({ prefix == "watch" }) { player: Player ->
    val bot = players.get(content)
    if (bot != null) {
        player.watch(bot)
    } else {
        player.clearWatch()
    }
}

command({ prefix == "shoot" }) { player: Player ->
    player.shoot("15", player.tile.addY(10))
}

command({ prefix == "face" }) { player: Player ->
    val parts = content.split(" ")
    player.turn(parts[0].toInt(), parts[1].toInt())
}

command({ prefix == "zone" || prefix == "chunk" }) { player: Player ->
    val zones: DynamicZones = get()
    zones.copy(player.tile.zone, player.tile.zone, rotation = 2)
}

command({ prefix == "clear_zone" }) { player: Player ->
    val zones: DynamicZones = get()
    zones.clear(player.tile.zone)
}

command({ prefix == "skill" }) { player: Player ->
    player.skillLevel = content.toInt()
}

command({ prefix == "cmb" }) { player: Player ->
    player.combatLevel = content.toInt()
}

command({ prefix == "tgl" }) { player: Player ->
    player.toggleSkillLevel()
}

command({ prefix == "sum" }) { player: Player ->
    player.summoningCombatLevel = content.toInt()
}