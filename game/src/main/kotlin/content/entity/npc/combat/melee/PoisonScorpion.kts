package content.entity.npc.combat.melee

import world.gregs.voidps.type.random
import content.entity.combat.npcCombatPrepare
import content.entity.effect.toxin.poison

npcCombatPrepare("poison_scorpion") { npc ->
    if (random.nextInt(2) == 0) { // Unknown rate
        val poison = npc.def["poison", 0]
        npc.poison(target, poison)
    }
}