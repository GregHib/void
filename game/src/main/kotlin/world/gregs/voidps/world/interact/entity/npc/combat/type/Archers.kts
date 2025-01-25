package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.world.interact.entity.combat.npcCombatPrepare
import content.skill.ranged.combat.ammo

npcCombatPrepare { npc ->
    npc.ammo = npc.def.getOrNull<String>("ammo") ?: return@npcCombatPrepare
}