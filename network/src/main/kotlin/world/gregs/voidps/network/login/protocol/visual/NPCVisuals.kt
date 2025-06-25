package world.gregs.voidps.network.login.protocol.visual

import world.gregs.voidps.network.login.protocol.visual.update.npc.Transformation

class NPCVisuals() : Visuals() {

    val transform = Transformation()
    var combatLevel: Int = -1
    var name: String = ""

    override fun reset() {
        super.reset()
        transform.clear()
        combatLevel = -1
        name = ""
    }
}