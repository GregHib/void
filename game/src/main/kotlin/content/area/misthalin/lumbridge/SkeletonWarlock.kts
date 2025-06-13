package content.area.misthalin.lumbridge

import content.entity.combat.killer
import world.gregs.voidps.engine.entity.npcDespawn

npcDespawn("skeleton_warlock") { npc ->
    npc.killer?.clear("restless_ghost_warlock")
}
