package world.gregs.voidps.world.activity.combat.consume.drink

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val Player.antifire: Boolean
    get() = get("antifire", 0) > 0

val Player.superAntifire: Boolean
    get() = get("super_antifire", 0) > 0

fun Player.antifire(minutes: Int) {
    set("antifire", TimeUnit.MINUTES.toTicks(minutes) / 30)
    timers.start("fire_resistance")
}

fun Player.superAntifire(minutes: Int) {
    set("super_antifire", TimeUnit.MINUTES.toTicks(minutes) / 20)
    timers.start("fire_immunity")
}
