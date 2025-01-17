package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext

abstract class Interaction<C : Character> : CancellableEvent(), SuspendableEvent, SuspendableContext<C> {
    var approach = false
    val operate: Boolean
        get() = !approach
    override var onCancel: (() -> Unit)? = null
    var launched = false

    abstract fun copy(approach: Boolean): Interaction<C>
}