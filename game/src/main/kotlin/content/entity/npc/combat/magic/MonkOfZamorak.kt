package content.entity.npc.combat.magic

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class MonkOfZamorak : Script {

    init {
        npcCombatPrepare("monk_of_zamorak*") { target ->
            spell = if (random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else ""
            true
        }
    }
}
