package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.player.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.player.effect.antifire
import world.gregs.voidps.world.interact.entity.player.effect.superAntifire

on<Consume>({ item.id.startsWith("antifire") || item.id.startsWith("antifire_mix") }) { player: Player ->
    player.antifire(6)
}

on<Consume>({ item.id.startsWith("super_antifire") }) { player: Player ->
    player.superAntifire(6)
}

on<Registered>({ it.antifire }) { player: Player ->
    player.timers.restart("fire_resistance")
}

on<Registered>({ it.superAntifire }) { player: Player ->
    player.timers.restart("fire_immunity")
}

on<TimerStart>({ timer == "fire_resistance" }) { _: Player ->
    interval = 30
}

on<TimerStart>({ timer == "fire_immunity" }) { _: Player ->
    interval = 20
}

on<TimerTick>({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    val remaining = player.dec(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
    if (remaining <= 0) {
        return@on cancel()
    }
    if (remaining == 1) {
        player.message("<dark_red>Your resistance to dragonfire is about to run out.")
    }
}

on<TimerStop>({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    player.message("<dark_red>Your resistance to dragonfire has run out.")
    player["antifire"] = 0
    player["super_antifire"] = 0
}