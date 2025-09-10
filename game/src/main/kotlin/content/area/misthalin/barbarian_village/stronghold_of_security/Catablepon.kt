package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.combat.npcCombatPrepare
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.event.Script
@Script
class Catablepon {

    init {
        npcCombatPrepare("catablepon*") { npc ->
            if (random.nextBoolean() && target.levels.get(Skill.Strength) > 3 + (target.levels.getMax(Skill.Strength) * 0.92)) {
                npc.anim("catablepon_attack_breath")
                npc.spell = "weaken"
            } else {
                npc.anim("catablepon_attack")
                npc.spell = ""
            }
        }

    }

}
