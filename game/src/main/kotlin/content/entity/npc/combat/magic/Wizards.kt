package content.entity.npc.combat.magic

import content.entity.combat.hit.directHit
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Wizards : Script {

    init {
        npcCombatPrepare {
            spell = def.getOrNull<String>("spell") ?: return@npcCombatPrepare true
            true
        }

        npcCombatPrepare("dark_wizard*") { target ->
            spell = if (random.nextBoolean() && Spell.canDrain(target, "confuse")) {
                if (def.combat < 20) "confuse" else "weaken"
            } else {
                if (def.combat < 20) "water_strike" else "earth_strike"
            }
            true
        }

        npcCombatPrepare("skeleton_mage*") { target ->
            if (def.combat == 16) {
                if (random.nextInt(4) == 0 && Spell.canDrain(target, "curse")) {
                    say("I infect your body with rot...")
                    spell = "curse"
                } else {
                    spell = ""
                }
            } else {
                if (random.nextInt(4) == 0 && Spell.canDrain(target, "vulnerability")) {
                    say("I infect your body with rot...")
                    spell = "vulnerability"
                } else {
                    spell = "fire_strike"
                }
            }
            true
        }

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
