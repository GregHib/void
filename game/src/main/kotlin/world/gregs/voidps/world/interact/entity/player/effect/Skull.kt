package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val Player.skulled: Boolean get() = skullCounter > 0

var Player.skullCounter: Int
    get() = getVar("skull_duration", 0)
    set(value) = setVar("skull_duration", value)

fun Player.skull(minutes: Int = 10, type: Int = 0) {
    setVar("skull", type)
    skullCounter = TimeUnit.MINUTES.toTicks(minutes) / 50
    softTimers.start("skull")
}

fun Player.unskull() {
    clearVar("skull")
    skullCounter = 0
    softTimers.stop("skull")
}