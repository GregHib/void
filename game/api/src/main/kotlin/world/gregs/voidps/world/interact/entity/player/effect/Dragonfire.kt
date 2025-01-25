package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit


val Character.antifire: Boolean
    get() = get("antifire", 0) > 0

val Character.superAntifire: Boolean
    get() = get("super_antifire", 0) > 0

fun Player.antifire(minutes: Int) {
    set("antifire", TimeUnit.MINUTES.toTicks(minutes) / 30)
    timers.start("fire_resistance")
}

fun Player.superAntifire(minutes: Int) {
    set("super_antifire", TimeUnit.MINUTES.toTicks(minutes) / 20)
    timers.start("fire_immunity")
}
