package rs.dusk.engine.model.entity.character.update.visual.player

import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MinimapHighlight(var highlighted: Boolean = false) : Visual

const val MINIMAP_HIGHLIGHT_MASK = 0x400

fun Player.flagMinimapHighlight() = visuals.flag(MINIMAP_HIGHLIGHT_MASK)

fun Player.getMinimapHighlight() = visuals.getOrPut(MINIMAP_HIGHLIGHT_MASK) { MinimapHighlight() }

var Player.minimapHighlight: Boolean
    get() = getMinimapHighlight().highlighted
    set(value) {
        getMinimapHighlight().highlighted = value
        flagMinimapHighlight()
    }