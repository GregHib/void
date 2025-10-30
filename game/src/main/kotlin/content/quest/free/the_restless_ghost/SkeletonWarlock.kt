package content.quest.free.the_restless_ghost

import content.entity.combat.killer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.npcDespawn

class SkeletonWarlock : Script {

    init {
        npcDespawn("skeleton_warlock") { npc ->
            npc.killer?.clear("restless_ghost_warlock")
        }
    }
}
