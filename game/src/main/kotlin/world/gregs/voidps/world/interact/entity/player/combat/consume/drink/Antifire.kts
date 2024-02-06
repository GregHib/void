package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import world.gregs.voidps.world.interact.entity.player.effect.antifire
import world.gregs.voidps.world.interact.entity.player.effect.superAntifire

consume({ item.id.startsWith("antifire") || item.id.startsWith("antifire_mix") }) { player: Player ->
    player.antifire(6)
}

consume({ item.id.startsWith("super_antifire") }) { player: Player ->
    player.superAntifire(6)
}

playerSpawn { player: Player ->
    if (player.antifire) {
        player.timers.restart("fire_resistance")
    }
    if (player.superAntifire) {
        player.timers.restart("fire_immunity")
    }
}

timerStart({ timer == "fire_resistance" }) { _: Player ->
    interval = 30
}

timerStart({ timer == "fire_immunity" }) { _: Player ->
    interval = 20
}

timerTick({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    val remaining = player.dec(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
    if (remaining <= 0) {
        cancel()
        return@timerTick
    }
    if (remaining == 1) {
        player.message("<dark_red>Your resistance to dragonfire is about to run out.")
    }
}

timerStop({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    player.message("<dark_red>Your resistance to dragonfire has run out.")
    player["antifire"] = 0
    player["super_antifire"] = 0
}