package world.gregs.voidps.network.visual

import world.gregs.voidps.network.visual.VisualMask.MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.visual.VisualMask.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.network.visual.update.Looks
import world.gregs.voidps.network.visual.update.player.Appearance
import world.gregs.voidps.network.visual.update.player.Face
import world.gregs.voidps.network.visual.update.player.MovementType
import world.gregs.voidps.network.visual.update.player.TemporaryMoveType

class PlayerVisuals(
    body: Looks
) : Visuals() {

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