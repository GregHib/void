package world.gregs.voidps.network.visual

import world.gregs.voidps.network.visual.update.npc.Transformation
import world.gregs.voidps.network.visual.update.npc.Turn

class NPCVisuals(index: Int) : Visuals(-index) {

    val transform = Transformation()
    val turn = Turn()

    override fun reset() {
        super.reset()
        transform.clear()
        turn.clear()
    }
}