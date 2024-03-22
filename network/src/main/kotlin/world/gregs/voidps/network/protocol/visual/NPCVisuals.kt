package world.gregs.voidps.network.protocol.visual

import world.gregs.voidps.network.protocol.visual.update.npc.Transformation

class NPCVisuals(index: Int) : Visuals(-index) {

    val transform = Transformation()

    override fun reset() {
        super.reset()
        transform.clear()
    }
}