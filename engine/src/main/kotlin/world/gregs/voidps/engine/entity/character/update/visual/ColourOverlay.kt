package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.network.visual.VisualMask.NPC_COLOUR_OVERLAY_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_COLOUR_OVERLAY_MASK

private fun mask(character: Character) = if (character is Player) PLAYER_COLOUR_OVERLAY_MASK else NPC_COLOUR_OVERLAY_MASK

fun Character.flagColourOverlay() = visuals.flag(mask(this))

fun Character.colourOverlay(colour: Int, delay: Int, duration: Int) {
    val overlay = visuals.colourOverlay
    overlay.colour = colour
    overlay.delay = delay
    overlay.duration = duration
    flagColourOverlay()
    start("colour_overlay", (delay + duration) / 30)
}