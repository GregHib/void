package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.utility.toTicks
import java.util.concurrent.TimeUnit

fun Player.skull(minutes: Int = 10, type: Int = 0) {
    this["skull", true] = type
    start("skull", TimeUnit.MINUTES.toTicks(minutes.toLong()), true)
}