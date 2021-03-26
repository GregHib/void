package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var source: Int = -1,// TODO source & target setting
    var target: Int = -1
) : Visual {
    override fun reset(character: Character) {
        hits.clear()
        source = -1
        target = -1
    }
}

const val PLAYER_HITS_MASK = 0x4

const val NPC_HITS_MASK = 0x40

fun Player.flagHits() = visuals.flag(PLAYER_HITS_MASK)

fun NPC.flagHits() = visuals.flag(NPC_HITS_MASK)

fun Player.getHits() = visuals.getOrPut(PLAYER_HITS_MASK) { Hits() }

fun NPC.getHits() = visuals.getOrPut(NPC_HITS_MASK) { Hits() }

fun Player.addHit(hit: Hit) {
    val hits = getHits()
    hits.hits.add(hit)
    flagHits()
}

fun NPC.addHit(hit: Hit) {
    val hits = getHits()
    hits.hits.add(hit)
    flagHits()
}
