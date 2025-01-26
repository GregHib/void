package content.area.misthalin.lumbridge

import world.gregs.voidps.engine.entity.npcDespawn
import content.entity.combat.killer

npcDespawn("skeleton_warlock") { npc ->
    npc.killer?.clear("restless_ghost_warlock")
}