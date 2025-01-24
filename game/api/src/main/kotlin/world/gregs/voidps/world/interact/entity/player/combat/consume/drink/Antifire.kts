package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.player.effect.antifire
import world.gregs.voidps.world.interact.entity.player.effect.superAntifire

playerSpawn { player ->
    if (player.antifire) {
        player.timers.restart("fire_resistance")
    }
    if (player.superAntifire) {
        player.timers.restart("fire_immunity")
    }
}

timerStart("fire_resistance") {
    interval = 30
}

timerStart("fire_immunity") {
    interval = 20
}

timerTick("fire_resistance", "fire_immunity") { player ->
    val remaining = player.dec(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
    if (remaining <= 0) {
        cancel()
        return@timerTick
    }
    if (remaining == 1) {
        player.message("<dark_red>Your resistance to dragonfire is about to run out.")
    }
}

timerStop("fire_resistance", "fire_immunity") { player ->
    player.message("<dark_red>Your resistance to dragonfire has run out.")
    player["antifire"] = 0
    player["super_antifire"] = 0
}