package content.entity.player

import world.gregs.voidps.engine.entity.character.mode.move.characterMove
import world.gregs.voidps.engine.event.Script

@Script
class ForceMovement {

    init {
        characterMove({ it.contains("force_walk") }) { character ->
            val block: () -> Unit = character.remove("force_walk") ?: return@characterMove
            block.invoke()
        }
    }
}
