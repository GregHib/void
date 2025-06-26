package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.VisualMask.MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.TEMPORARY_MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance
import world.gregs.voidps.network.login.protocol.visual.update.player.Body
import world.gregs.voidps.network.login.protocol.visual.update.player.MovementType

class PlayerVisuals(
    body: Body,
) : Visuals() {

    val movementType = MovementType()
    val appearance = Appearance(body = body)
    val temporaryMoveType = MovementType()

    override fun reset() {
        super.reset()
        if (movementType.needsReset()) {
            flag(MOVEMENT_TYPE_MASK)
            movementType.reset()
        }
        appearance.clear()
        if (temporaryMoveType.needsReset()) {
            flag(TEMPORARY_MOVEMENT_TYPE_MASK)
            temporaryMoveType.reset()
        }
    }
}
