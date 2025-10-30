package content.entity.npc.combat.ranged

import content.entity.combat.npcCombatPrepare
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script

class Archers : Script {

    init {
        npcCombatPrepare { npc ->
            npc.ammo = npc.def.getOrNull<String>("ammo") ?: return@npcCombatPrepare
        }
    }
}
