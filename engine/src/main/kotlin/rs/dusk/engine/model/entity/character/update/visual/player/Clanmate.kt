package rs.dusk.engine.model.entity.character.update.visual.player

import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Clanmate(var clanmate: Boolean = false) : Visual

const val CLANMATE_MASK = 0x100000

fun Player.flagClanmate() = visuals.flag(CLANMATE_MASK)

private fun Player.getClanmate() = visuals.getOrPut(CLANMATE_MASK) { Clanmate() }

var Player.clanmate: Boolean
    get() = getClanmate().clanmate
    set(value) {
        getClanmate().clanmate = value
        flagClanmate()
    }