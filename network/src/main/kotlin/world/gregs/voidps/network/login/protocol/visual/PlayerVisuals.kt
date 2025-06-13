package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.VisualMask.MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance
import world.gregs.voidps.network.login.protocol.visual.update.player.Body
import world.gregs.voidps.network.login.protocol.visual.update.player.MovementType
import world.gregs.voidps.network.login.protocol.visual.update.player.TemporaryMoveType

class PlayerVisuals(
    body: Body,
) : Visuals() {

    val temporaryMoveType = TemporaryMoveType()
    val appearance = Appearance(body = body)
    val movementType = MovementType()

    override fun reset() {
        super.reset()
        if (temporaryMoveType.needsReset()) {
            flag(TEMPORARY_MOVE_TYPE_MASK)
            temporaryMoveType.reset()
        }
        appearance.clear()
        if (movementType.needsReset()) {
            flag(MOVEMENT_TYPE_MASK)
            movementType.reset()
        }
    }
}
