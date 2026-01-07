package content.entity.npc.combat.magic

import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class DarkWizards : Script {

    init {
        npcCondition("not_confused") { target -> Spell.canDrain(target, "confuse") }
        npcCondition("not_weakened") { target -> Spell.canDrain(target, "weaken") }
    }
}
