package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class ColourOverlay(
    var delay: Int = 0,
    var duration: Int = 0,
    var colour: Int = 0
) : Visual

const val PLAYER_COLOUR_OVERLAY_MASK = 0x40000
const val NPC_COLOUR_OVERLAY_MASK = 0x2000

private fun mask(character: Character) = if (character is Player) PLAYER_COLOUR_OVERLAY_MASK else NPC_COLOUR_OVERLAY_MASK

fun Character.flagColourOverlay() = visuals.flag(mask(this))

val Character.colourOverlay: ColourOverlay
    get() = visuals.colourOverlay