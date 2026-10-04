package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_ANIMATION_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_1_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_GRAPHIC_2_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_HITS_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_SAY_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.NPC_TIME_BAR_MASK
import world.gregs.voidps.network.login.protocol.visual.VisualMask.TRANSFORM_MASK
import world.gregs.voidps.network.login.protocol.visual.update.npc.Transformation

class NPCVisuals : Visuals() {

    val transform = Transformation()

    override fun reset() {
        val flag = flag
        if (flag == 0 && !moved && !tele && walkStep == -1 && runStep == -1) {
            return
        }
        super.reset()
        if (flag and NPC_ANIMATION_MASK != 0) {
            animation.clear()
        }
        if (flag and NPC_GRAPHIC_1_MASK != 0) {
            primaryGraphic.clear()
        }
        if (flag and NPC_GRAPHIC_2_MASK != 0) {
            secondaryGraphic.clear()
        }
        if (flag and NPC_HITS_MASK != 0) {
            hits.clear()
        }
        if (flag and NPC_SAY_MASK != 0) {
            say.clear()
        }
        if (flag and NPC_TIME_BAR_MASK != 0) {
            timeBar.clear()
        }
        if (flag and TRANSFORM_MASK != 0) {
            transform.clear()
        }
    }
}
