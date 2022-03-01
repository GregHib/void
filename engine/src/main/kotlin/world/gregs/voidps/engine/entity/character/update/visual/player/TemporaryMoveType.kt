package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.client.update.task.MoveType
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class TemporaryMoveType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(): Boolean {
        return type != MoveType.None
    }

    override fun reset() {
        type = MoveType.None
    }
}

const val TEMPORARY_MOVE_TYPE_MASK = 0x80

fun Player.flagTemporaryMoveType() = visuals.flag(TEMPORARY_MOVE_TYPE_MASK)

var Player.temporaryMoveType: MoveType
    get() = visuals.temporaryMoveType.type
    set(value) {
        if (visuals.temporaryMoveType.type != value) {
            visuals.temporaryMoveType.type = value
            flagTemporaryMoveType()
        }
    }