package content.quest.free.the_restless_ghost

import content.entity.combat.killer
import world.gregs.voidps.engine.entity.npcDespawn
import world.gregs.voidps.engine.event.Script
@Script
class SkeletonWarlock {

    init {
        npcDespawn("skeleton_warlock") { npc ->
            npc.killer?.clear("restless_ghost_warlock")
        }

    }

}
