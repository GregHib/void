package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.WarningRed
import world.gregs.voidps.engine.client.variable.decVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.combat.consume.Consume
import java.util.concurrent.TimeUnit

on<Consume>({ item.id.startsWith("antifire") || item.id.startsWith("antifire_mix") }) { player: Player ->
    player.setVar("antifire", TimeUnit.MINUTES.toTicks(6) / 30)
    player.timers.start("fire_resistance")
}

on<Consume>({ item.id.startsWith("super_antifire") }) { player: Player ->
    player.setVar("super_antifire", TimeUnit.MINUTES.toTicks(6) / 20)
    player.timers.start("fire_immunity")
}

on<Registered>({ it.getVar("antifire", 0) > 0 }) { player: Player ->
    player.timers.restart("fire_resistance")
}

on<Registered>({ it.getVar("super_antifire", 0) > 0 }) { player: Player ->
    player.timers.restart("fire_immunity")
}

on<TimerStart>({ timer == "fire_resistance" }) { _: Player ->
    interval = 30
}

on<TimerStart>({ timer == "fire_immunity" }) { _: Player ->
    interval = 20
}

on<TimerTick>({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    val remaining = player.decVar(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
    if (remaining <= 0) {
        return@on cancel()
    }
    if (remaining == 1) {
        player.message(WarningRed { "Your resistance to dragonfire is about to run out." })
    }
}

on<TimerStop>({ timer == "fire_resistance" || timer == "fire_immunity" }) { player: Player ->
    player.message(WarningRed { "Your resistance to dragonfire has run out." })
}