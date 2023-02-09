package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.event.Event

object StopInteraction : Event

fun Character.clear(queue: Boolean = true, mode: Boolean = true, suspend: Boolean = true, animation: Boolean = true, dialogue: Boolean = true) {
    if (queue) {
        this.queue.clearWeak()
    }
    if (mode) {
        this.mode = EmptyMode
    }
    if (suspend) {
        if (dialogue || this.suspension?.dialogue != true) {
            this.suspension = null
        }
    }
    if (animation) {
        clearAnimation()
    }
    events.emit(StopInteraction)
}