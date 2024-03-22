package world.gregs.voidps.network.protocol.visual.update.npc

import world.gregs.voidps.network.protocol.Visual

/**
 * Changes the characteristics to match NPC with [id]
 */
data class Transformation(var id: Int = -1) : Visual {
    override fun reset() {
        id = -1
    }
}