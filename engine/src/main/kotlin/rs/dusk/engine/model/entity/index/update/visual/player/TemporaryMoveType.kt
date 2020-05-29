package rs.dusk.engine.model.entity.index.update.visual.player

import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class TemporaryMoveType(var type: PlayerMoveType = PlayerMoveType.None) : Visual {
    override fun reset(indexed: Indexed) {
        val player = indexed as Player
        player.temporaryMoveType = PlayerMoveType.None
    }
}

const val TEMPORARY_MOVE_TYPE_MASK = 0x1

fun Player.flagTemporaryMoveType() = visuals.flag(TEMPORARY_MOVE_TYPE_MASK)

fun Player.getTemporaryMoveType() = visuals.getOrPut(TEMPORARY_MOVE_TYPE_MASK) { TemporaryMoveType() }

var Player.temporaryMoveType: PlayerMoveType
    get() = getTemporaryMoveType().type
    set(value) {
        if (getTemporaryMoveType().type != value) {
            getTemporaryMoveType().type = value
            flagTemporaryMoveType()
        }
    }