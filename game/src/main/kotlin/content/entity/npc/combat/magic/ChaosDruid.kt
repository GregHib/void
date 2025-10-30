package content.entity.npc.combat.magic

import content.entity.combat.npcCombatPrepare
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class ChaosDruid : Script {

    init {
        npcCombatPrepare("chaos_druid*") { npc ->
            npc.spell = if (random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else ""
        }
    }
}
