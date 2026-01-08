package content.entity.npc.combat.magic

import content.entity.combat.hit.directHit
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script

class Wizards : Script {

    init {
        npcCondition("not_confused") { target -> Spell.canDrain(target, "confuse") }
        npcCondition("not_weakened") { target -> Spell.canDrain(target, "weaken") }
        npcCondition("not_cursed") { target -> Spell.canDrain(target, "curse") }
        npcCondition("not_vulnerable") { target -> Spell.canDrain(target, "vulnerability") }

        npcCombatDamage("air_wizard") {
            if (it.spell.startsWith("air_")) {
                directHit(it.damage, "healed")
            }
        }

        npcCombatDamage("water_wizard") {
            if (it.spell.startsWith("water_")) {
                directHit(it.damage, "healed")
            }
        }

        npcCombatDamage("earth_wizard") {
            if (it.spell.startsWith("earth_")) {
                directHit(it.damage, "healed")
            }
        }

        npcCombatDamage("fire_wizard") {
            if (it.spell.startsWith("fire_")) {
                directHit(it.damage, "healed")
            }
        }
    }
}
