package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.visual.MoveType
import world.gregs.voidps.network.visual.VisualMask.TEMPORARY_MOVE_TYPE_MASK

fun Player.flagTemporaryMoveType() = visuals.flag(TEMPORARY_MOVE_TYPE_MASK)

var Player.temporaryMoveType: MoveType
    get() = visuals.temporaryMoveType.type
    set(value) {
        if (visuals.temporaryMoveType.type != value) {
            visuals.temporaryMoveType.type = value
            flagTemporaryMoveType()
        }
    }