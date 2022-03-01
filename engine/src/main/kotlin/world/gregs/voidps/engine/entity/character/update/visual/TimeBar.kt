package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.visual.VisualMask.NPC_TIME_BAR_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_TIME_BAR_MASK

private fun mask(character: Character) = if (character is Player) PLAYER_TIME_BAR_MASK else NPC_TIME_BAR_MASK

fun Character.flagTimeBar() = visuals.flag(mask(this))

fun Character.setTimeBar(full: Boolean = false, exponentialDelay: Int = 0, delay: Int = 0, increment: Int = 0) {
    val bar = visuals.timeBar
    bar.full = full
    bar.exponentialDelay = exponentialDelay
    bar.delay = delay
    bar.increment = increment
    flagTimeBar()
}