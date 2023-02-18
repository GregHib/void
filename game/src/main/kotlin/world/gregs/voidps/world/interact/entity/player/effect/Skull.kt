package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

fun Player.skull(minutes: Int = 10, type: Int = 0) {
    setVar("skull", type)
    setVar("skull_duration", TimeUnit.MINUTES.toTicks(minutes))
    softTimers.start("skull")
}