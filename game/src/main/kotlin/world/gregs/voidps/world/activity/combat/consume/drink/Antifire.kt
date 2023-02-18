package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val Player.antifire: Boolean
    get() = getVar("antifire", 0) > 0

val Player.superAntifire: Boolean
    get() = getVar("super_antifire", 0) > 0

fun Player.antifire(minutes: Int) {
    setVar("antifire", TimeUnit.MINUTES.toTicks(minutes) / 30)
    timers.start("fire_resistance")
}

fun Player.superAntifire(minutes: Int) {
    setVar("super_antifire", TimeUnit.MINUTES.toTicks(minutes) / 20)
    timers.start("fire_immunity")
}
