package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class Hits(
    val hits: MutableList<Hitsplat> = mutableListOf(),
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