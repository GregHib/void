package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent

abstract class Interaction : CancellableEvent(), SuspendableEvent, CharacterContext {
    var approach = false
    val operate: Boolean
        get() = !approach
    override var onCancel: (() -> Unit)? = null

    abstract fun copy(approach: Boolean): Interaction
}