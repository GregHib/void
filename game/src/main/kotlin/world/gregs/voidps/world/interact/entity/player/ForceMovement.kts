package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.remove
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.event.on

on<Moved>({ it.contains("force_walk") }) { character: Character ->
    val block: () -> Unit = character.remove("force_walk") ?: return@on
    block.invoke()
}