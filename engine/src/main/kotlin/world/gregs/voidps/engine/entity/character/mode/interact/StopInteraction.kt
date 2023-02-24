package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.event.Event

object StopInteraction : Event

fun Character.clearInteract(queue: Boolean = true, mode: Boolean = true, suspend: Boolean = true, dialogue: Boolean = true) {
    if (queue) {
        this.queue.clearWeak()
    }
    if (mode) {
        this.mode = EmptyMode
    }
    if (suspend && (dialogue || this.suspension?.dialogue != true)) {
        this.suspension?.cancel()
        this.suspension = null
    }
    events.emit(StopInteraction)
}