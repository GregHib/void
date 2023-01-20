package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.addEvent
import world.gregs.voidps.engine.event.suspend.EventSuspension

@Suppress("SuspiciousVarProperty")
data class Approach<T : SuspendableEvent>(val event: T) : SuspendableEvent() {
    override var suspend: EventSuspension?
        get() = event.suspend
        set(value) {
            event.suspend = value
        }
    override var suspended: Boolean = false
        get() = event.suspended
}

inline fun <reified E : SuspendableEvent> onApproach(
    noinline condition: E.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend E.(player: Player) -> Unit
) = addEvent<Player, Approach<E>>({ condition.invoke(event, it) }, priority) { block.invoke(event, it) }