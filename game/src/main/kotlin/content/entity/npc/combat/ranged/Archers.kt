package content.entity.npc.combat.ranged

import content.entity.combat.npcCombatPrepare
import content.skill.ranged.ammo
import world.gregs.voidps.engine.event.Script
@Script
class Archers {

    init {
        npcCombatPrepare { npc ->
            npc.ammo = npc.def.getOrNull<String>("ammo") ?: return@npcCombatPrepare
        }

    }

}
