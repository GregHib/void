package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var source: Int = -1,// TODO source & target setting
    var target: Int = -1
) : Visual {
    override fun needsReset(character: Character): Boolean {
        return hits.isNotEmpty()
    }

    override fun reset(character: Character) {
        hits.clear()
        source = -1
        target = -1
    }
}

const val PLAYER_HITS_MASK = 0x4

const val NPC_HITS_MASK = 0x40

private fun mask(character: Character) = if (character is Player) PLAYER_HITS_MASK else NPC_HITS_MASK

fun Character.flagHits() = visuals.flag(mask(this))

fun Character.getHits() = visuals.getOrPut(mask(this)) { Hits() }

fun Character.addHit(hit: Hit) {
    val hits = getHits()
    hits.hits.add(hit)
    flagHits()
}
