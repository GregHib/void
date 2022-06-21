package world.gregs.voidps.network.visual

import world.gregs.voidps.network.visual.VisualMask.MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.visual.VisualMask.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.network.visual.update.player.*

class PlayerVisuals(
    index: Int,
    body: Body
) : Visuals(index) {

    val face = Face()
    val temporaryMoveType = TemporaryMoveType()
    val appearance = Appearance(body = body)
    val movementType = MovementType()

    override fun reset() {
        super.reset()
        face.clear()
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