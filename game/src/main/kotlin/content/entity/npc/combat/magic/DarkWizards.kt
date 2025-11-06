package content.entity.npc.combat.magic

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class DarkWizards : Script {

    init {
        npcCombatPrepare("dark_wizard_water*") { target ->
            spell = if (!random.nextBoolean() && Spell.canDrain(target, "confuse")) "confuse" else "water_strike"
            true
        }

        npcCombatPrepare("dark_wizard_earth*") { target ->
            spell = if (!random.nextBoolean() && Spell.canDrain(target, "weaken")) "weaken" else "earth_strike"
            true
        }
    }
}
