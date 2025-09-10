package content.entity.npc.combat.melee

import content.entity.combat.npcCombatPrepare
import content.entity.effect.toxin.poison
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.event.Script
@Script
class Poisonous {

    init {
        npcCombatPrepare { npc ->
            val damage = npc.def.getOrNull<Int>("poison") ?: return@npcCombatPrepare
            val roll = npc.def["poison_roll", 0]
            if (roll == 0) {
                npc.poison(target, damage)
                return@npcCombatPrepare
            }
            if (random.nextInt(roll) == 0) {
                npc.poison(target, damage)
            }
        }

    }

}
