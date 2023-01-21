package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.addEvent

data class Approach<T : SuspendableEvent>(val event: T) : SuspendableEvent()

inline fun <reified E : SuspendableEvent> onApproach(
    noinline condition: E.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend E.(player: Player) -> Unit
) = addEvent<Player, Approach<E>>({ condition.invoke(event, it) }, priority) { block.invoke(event, it) }