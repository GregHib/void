package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.Visual

data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var self: Int = 0,
    var target: Int = 0
) : Visual {
    override fun needsReset(): Boolean {
        return hits.isNotEmpty()
    }

    override fun reset() {
        hits.clear()
        target = 0
    }
}