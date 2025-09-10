package content.entity.npc.combat.magic

import content.entity.combat.npcCombatPrepare
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class MonkOfZamorak {

    init {
        npcCombatPrepare("monk_of_zamorak*") { npc ->
            npc.spell = if (random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else ""
        }
    }
}
