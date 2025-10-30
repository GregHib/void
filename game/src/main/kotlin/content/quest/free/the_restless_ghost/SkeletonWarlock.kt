package content.quest.free.the_restless_ghost

import content.entity.combat.killer
import world.gregs.voidps.engine.Script

class SkeletonWarlock : Script {

    init {
        npcDespawn("skeleton_warlock") {
            killer?.clear("restless_ghost_warlock")
        }
    }
}
