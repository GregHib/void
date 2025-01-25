package content.skill.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat

fun multiTargets(target: Character, hits: Int): List<Character> {
    val group = if (target is Player) get<Players>() else get<NPCs>()
    val targets = mutableListOf<Character>()
    for (tile in target.tile.spiral(1)) {
        val characters = group[tile]
        for (character in characters) {
            if (character == target || !character.inMultiCombat) {
                continue
            }
            targets.add(character)
            if (targets.size >= hits) {
                return targets
            }
        }
    }
    return targets
}

fun drainByDamage(target: Character, damage: Int, vararg skills: Skill) {
    if (damage == -1) {
        return
    }
    var drain = damage / 10
    if (drain > 0) {
        for (skill in skills) {
            val current = target.levels.get(skill)
            if (current <= 1) {
                continue
            }
            target.levels.drain(skill, drain)
            drain -= current
            if (drain <= 0) {
                break
            }
        }
    }
}