package content.entity.npc.combat.ranged

import content.entity.combat.npcCombatPrepare
import content.skill.ranged.ammo

npcCombatPrepare { npc ->
    npc.ammo = npc.def.getOrNull<String>("ammo") ?: return@npcCombatPrepare
}