package content.entity.npc.combat.melee

import content.entity.effect.toxin.poison
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Poisonous : Script {

    init {
        npcCombatPrepare { target ->
            val damage = def.getOrNull<Int>("poison") ?: return@npcCombatPrepare true
            val roll = def["poison_roll", 0]
            if (roll == 0) {
                poison(target, damage)
                return@npcCombatPrepare true
            }
            if (random.nextInt(roll) == 0) {
                poison(target, damage)
            }
            true
        }
    }
}
