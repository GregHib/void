package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.npcDespawn
import world.gregs.voidps.world.interact.entity.combat.killer

npcDespawn("skeleton_warlock") { npc ->
    npc.killer?.clear("restless_ghost_warlock")
}