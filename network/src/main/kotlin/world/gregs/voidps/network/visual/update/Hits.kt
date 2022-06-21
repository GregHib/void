package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.Visual

data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var self: Int = -1,
    var target: Int = -1
) : Visual {
    override fun needsReset(): Boolean {
        return hits.isNotEmpty()
    }

    override fun reset() {
        hits.clear()
        target = -1
    }
}