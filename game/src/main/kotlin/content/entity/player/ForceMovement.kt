package content.entity.player

import world.gregs.voidps.engine.Script

class ForceMovement : Script {

    init {
        moved {
            val block: () -> Unit = remove("force_walk") ?: return@moved
            block.invoke()
        }

        npcMoved {
            val block: () -> Unit = remove("force_walk") ?: return@npcMoved
            block.invoke()
        }
    }
}
