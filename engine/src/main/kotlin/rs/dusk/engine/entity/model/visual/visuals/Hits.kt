package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Hit
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var source: Int = -1,// TODO source & target setting
    var target: Int = -1
) : Visual

fun Player.flagHits() = visuals.flag(0x4)

fun NPC.flagHits() = visuals.flag(0x40)

fun Indexed.flagHits() {
    if (this is Player) flagHits() else if (this is NPC) flagHits()
}

fun Indexed.getHits() = visuals.getOrPut(Hits::class) { Hits() }

fun Indexed.addHit(hit: Hit) {
    val hits = getHits()
    hits.hits.add(hit)
    flagHits()
}
