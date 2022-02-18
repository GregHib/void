import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.event.on

on<Moved>({ it.contains("force_walk") }) { character: Character ->
    val block: () -> Unit = character.remove("force_walk") ?: return@on
    block.invoke()
}