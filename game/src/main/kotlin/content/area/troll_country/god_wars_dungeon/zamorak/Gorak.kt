package content.area.troll_country.god_wars_dungeon.zamorak

import content.entity.combat.hit.combatDamage
import content.entity.combat.hit.npcCombatAttack
import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class Gorak {

    val skills = Skill.entries.toMutableSet().apply {
        remove(Skill.Constitution)
    }

    init {
        npcCombatAttack("gorak*") {
            if (target is Player && damage > 0) {
                target.levels.drain(skills.random(random), random.nextInt(1, 4))
            }
        }

        combatDamage { player ->
            if (source is NPC && source.id.startsWith("gorak") && player.protectMelee()) {
                player.message("Your protective prayer doesn't seem to work!")
            }
        }
    }
}
