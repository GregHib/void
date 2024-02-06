package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.characterMove

characterMove({ it.contains("force_walk") }) { character: Character ->
    val block: () -> Unit = character.remove("force_walk") ?: return@characterMove
    block.invoke()
}