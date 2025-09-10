package content.entity.npc.combat.magic

import content.entity.combat.npcCombatPrepare
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class DarkWizards {

    init {
        npcCombatPrepare("dark_wizard_water*") { npc ->
            npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
        }

        npcCombatPrepare("dark_wizard_earth*") { npc ->
            npc.spell = if (!random.nextBoolean() && Spell.canDrain(target, "weaken")) "weaken" else "earth_strike"
        }
    }
}
