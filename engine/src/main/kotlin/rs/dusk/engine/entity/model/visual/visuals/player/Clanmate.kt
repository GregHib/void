package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Clanmate(var clanmate: Boolean = false) : Visual

fun Player.getClanmate() = visuals.getOrPut(Clanmate::class) { Clanmate() }

fun Player.flagClanmate() = visuals.flag(0x100000)

fun Player.setClanmate(clanmate: Boolean = false) {
    val mate = getClanmate()
    mate.clanmate = clanmate
    flagClanmate()
}