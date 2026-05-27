package content.quest.free.the_restless_ghost

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Players

class SkeletonWarlock : Script {

    init {
        npcDespawn("skeleton_warlock") {
            val accountName = get<String>("owner") ?: return@npcDespawn
            val player = Players.findByAccount(accountName) ?: return@npcDespawn
            player.clear("restless_ghost_warlock")
        }
    }
}
