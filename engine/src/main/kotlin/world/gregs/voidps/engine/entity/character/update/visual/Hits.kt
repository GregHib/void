package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.visual.VisualMask.NPC_HITS_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_HITS_MASK
import world.gregs.voidps.network.visual.update.Hit

private fun mask(character: Character) = if (character is Player) PLAYER_HITS_MASK else NPC_HITS_MASK

fun Character.flagHits() = visuals.flag(mask(this))

fun Character.addHit(hit: Hit) {
    visuals.hits.hits.add(hit)
    flagHits()
}

fun Character.hit(source: Character, amount: Int, mark: Hit.Mark, delay: Int = 0, critical: Boolean = false, soak: Int = -1) {
    val health = levels.get(Skill.Constitution)
    val max = levels.getMax(Skill.Constitution).toDouble()
    val percentage = (((health - amount).coerceAtLeast(0) / max) * 255).toInt()
    addHit(Hit(amount, mark, percentage, delay, critical, if (source is NPC) -source.index else source.index, soak))
}
