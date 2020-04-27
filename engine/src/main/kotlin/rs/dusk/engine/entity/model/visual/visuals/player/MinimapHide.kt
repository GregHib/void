package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MinimapHide(var hidden: Boolean = false) : Visual

const val MINIMAP_HIDE_MASK = 0x400

fun Player.flagMinimapHide() = visuals.flag(MINIMAP_HIDE_MASK)

fun Player.getMinimapHide() = visuals.getOrPut(MINIMAP_HIDE_MASK) { MinimapHide() }

var Player.minimapHidden: Boolean
    get() = getMinimapHide().hidden
    set(value) {
        getMinimapHide().hidden = value
        flagMinimapHide()
    }