package content.quest.free.the_restless_ghost

import content.entity.combat.killer
import world.gregs.voidps.engine.entity.npcDespawn

npcDespawn("skeleton_warlock") { npc ->
    npc.killer?.clear("restless_ghost_warlock")
}
