package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class TemporaryMoveType(var type: PlayerMoveType = PlayerMoveType.None) : Visual {
    override fun reset(character: Character) {
        val player = character as Player
        player.temporaryMoveType = PlayerMoveType.None
    }
}

const val TEMPORARY_MOVE_TYPE_MASK = 0x80

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