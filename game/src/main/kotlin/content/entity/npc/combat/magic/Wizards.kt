package content.entity.npc.combat.magic

import content.entity.combat.hit.directHit
import content.entity.combat.hit.npcCombatDamage
import content.entity.combat.npcCombatPrepare
import content.skill.magic.spell.Spell
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class Wizards {

    init {
        npcCombatPrepare { npc ->
            npc.spell = npc.def.getOrNull<String>("spell") ?: return@npcCombatPrepare
        }

        npcCombatPrepare("dark_wizard*") { npc ->
            if (random.nextBoolean() && Spell.canDrain(target, "confuse")) {
                npc.spell = if (npc.def.combat < 20) "confuse" else "weaken"
            } else {
                npc.spell = if (npc.def.combat < 20) "water_strike" else "earth_strike"
            }
        }

        npcCombatPrepare("skeleton_mage*") { npc ->
            if (npc.def.combat == 16) {
                if (random.nextInt(4) == 0 && Spell.canDrain(target, "curse")) {
                    npc.say("I infect your body with rot...")
                    npc.spell = "curse"
                } else {
                    npc.spell = ""
                }
            } else {
                if (random.nextInt(4) == 0 && Spell.canDrain(target, "vulnerability")) {
                    npc.say("I infect your body with rot...")
                    npc.spell = "vulnerability"
                } else {
                    npc.spell = "fire_strike"
                }
            }
        }

        npcCombatDamage("air_wizard", spell = "air_*") { npc ->
            npc.directHit(damage, "healed")
        }

        npcCombatDamage("water_wizard", spell = "water_*") { npc ->
            npc.directHit(damage, "healed")
        }

        npcCombatDamage("earth_wizard", spell = "earth_*") { npc ->
            npc.directHit(damage, "healed")
        }

        npcCombatDamage("fire_wizard", spell = "fire_*") { npc ->
            npc.directHit(damage, "healed")
        }
    }
}
