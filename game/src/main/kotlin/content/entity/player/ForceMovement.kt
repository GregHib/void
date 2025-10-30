package content.entity.player

import world.gregs.voidps.engine.Script

class ForceMovement : Script {
    init {
        moved { player, _ ->
            val block: () -> Unit = player.remove("force_walk") ?: return@moved
            block.invoke()
        }

        npcMoved { npc, _ ->
            val block: () -> Unit = npc.remove("force_walk") ?: return@npcMoved
            block.invoke()
        }
    }
}
