package world.gregs.voidps.network.visual.update.npc

import world.gregs.voidps.network.Visual

/**
 * Changes the characteristics to match NPC with [id]
 */
data class Transformation(var id: Int = -1) : Visual {
    override fun reset() {
        id = -1
    }
}