package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.update.npc.Transformation

class NPCVisuals(index: Int) : Visuals(-index) {

    val transform = Transformation()

    override fun reset() {
        super.reset()
        transform.clear()
    }
}