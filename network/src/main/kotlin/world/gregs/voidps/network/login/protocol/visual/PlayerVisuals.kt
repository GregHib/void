package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.VisualMask.MOVEMENT_TYPE_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_ANIMATION_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_GRAPHIC_1_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_GRAPHIC_2_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_HITS_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_SAY_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.PLAYER_TIME_BAR_MASK
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
        val flag = flag
        if (flag == 0 && !moved && !tele && walkStep == -1 && runStep == -1) {
            return
        }
        super.reset()
        if (flag and PLAYER_ANIMATION_MASK != 0) {
            animation.clear()
        }
        if (flag and PLAYER_GRAPHIC_1_MASK != 0) {
            primaryGraphic.clear()
        }
        if (flag and PLAYER_GRAPHIC_2_MASK != 0) {
            secondaryGraphic.clear()
        }
        if (flag and PLAYER_HITS_MASK != 0) {
            hits.clear()
        }
        if (flag and PLAYER_SAY_MASK != 0) {
            say.clear()
        }
        if (flag and PLAYER_TIME_BAR_MASK != 0) {
            timeBar.clear()
        }
        if (flag and MOVEMENT_TYPE_MASK != 0 && movementType.needsReset()) {
            flag(MOVEMENT_TYPE_MASK)
            movementType.reset()
        }
        if (flag and TEMPORARY_MOVEMENT_TYPE_MASK != 0 && temporaryMoveType.needsReset()) {
            flag(TEMPORARY_MOVEMENT_TYPE_MASK)
            temporaryMoveType.reset()
        }
    }
}
