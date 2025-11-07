package content.entity.npc.combat.ranged

import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script

class Archers : Script {

    init {
        npcCombatPrepare {
            ammo = def.getOrNull<String>("ammo") ?: return@npcCombatPrepare true
            true
        }
    }
}
