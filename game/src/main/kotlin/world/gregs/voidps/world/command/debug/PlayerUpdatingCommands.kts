package world.gregs.voidps.world.command.debug

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.modCommand
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

adminCommand("kill") {
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

modCommand("players") {
    player.message("Players: ${players.size}, ${player.viewport?.players?.localCount}")
}

adminCommand("under") {
    players[player.tile].forEach {
        println("$it - ${players[it.tile]}")
    }
}

adminCommand("anim") {
    when (content) {
        "-1", "" -> player.clearAnimation()
        else -> player.setAnimation(content, override = true)// 863
    }
}

adminCommand("emote") {
    when (content) {
        "-1", "" -> player.renderEmote = "human_stand"
        else -> player.renderEmote = content
    }
}

adminCommand("gfx") {
    when (content) {
        "-1", "" -> player.clearGraphic()
        else -> player.setGraphic(content)// 93
    }
}

adminCommand("proj") {
    player.shoot(content, player.tile.add(0, 5), delay = 0, flightTime = 400)
}

adminCommand("tfm", "transform") {
    player.transform(content)
}

adminCommand("overlay") {
    player.colourOverlay(-2108002746, 10, 100)
}

adminCommand("chat") {
    player.forceChat = content
}

adminCommand("move") {
    player.setExactMovement(Delta(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

adminCommand("hit") {
    player.damage(content.toIntOrNull() ?: 10)
}

adminCommand("time") {
    player.setTimeBar(true, 0, 60, 1)
}

adminCommand("watch") {
    val bot = players.get(content)
    if (bot != null) {
        player.watch(bot)
    } else {
        player.clearWatch()
    }
}

adminCommand("shoot") {
    player.shoot("15", player.tile.addY(10))
}

adminCommand("face") {
    val parts = content.split(" ")
    player.turn(parts[0].toInt(), parts[1].toInt())
}

adminCommand("zone", "chunk") {
    val zones: DynamicZones = get()
    zones.copy(player.tile.zone, player.tile.zone, rotation = 2)
}

adminCommand("clear_zone") {
    val zones: DynamicZones = get()
    zones.clear(player.tile.zone)
}

adminCommand("skill") {
    player.skillLevel = content.toInt()
}

adminCommand("cmb") {
    player.combatLevel = content.toInt()
}

adminCommand("tgl") {
    player.toggleSkillLevel()
}

adminCommand("sum") {
    player.summoningCombatLevel = content.toInt()
}