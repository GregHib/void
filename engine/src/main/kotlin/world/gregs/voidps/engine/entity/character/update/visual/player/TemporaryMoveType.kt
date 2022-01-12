package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.client.update.task.MoveType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class TemporaryMoveType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(character: Character): Boolean {
        return type != MoveType.None
    }

    override fun reset(character: Character) {
        val player = character as Player
        player.temporaryMoveType = MoveType.None
    }
}

const val TEMPORARY_MOVE_TYPE_MASK = 0x80

fun Player.flagTemporaryMoveType() = visuals.flag(TEMPORARY_MOVE_TYPE_MASK)

fun Player.getTemporaryMoveType() = visuals.getOrPut(TEMPORARY_MOVE_TYPE_MASK) { TemporaryMoveType() }

var Player.temporaryMoveType: MoveType
    get() = getTemporaryMoveType().type
    set(value) {
        if (getTemporaryMoveType().type != value) {
            getTemporaryMoveType().type = value
            flagTemporaryMoveType()
        }
    }