package content.entity.player

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.event.Script

@Script
class ForceMovement : Api {
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
