package world.gregs.voidps.network.visual.update

import world.gregs.voidps.network.Visual

data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var source: Int = -1,// TODO source & target setting
    var target: Int = -1
) : Visual {
    override fun needsReset(): Boolean {
        return hits.isNotEmpty()
    }

    override fun reset() {
        hits.clear()
        source = -1
        target = -1
    }
}