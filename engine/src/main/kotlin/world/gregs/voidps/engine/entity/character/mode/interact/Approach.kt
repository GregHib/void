package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.addEvent

inline fun <reified E : Interaction> onApproach(
    noinline condition: E.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend E.(player: Player) -> Unit
) = addEvent({ approach && condition(this, it) }, priority, block)