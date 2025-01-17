package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent

abstract class Interaction<C : Character> : CancellableEvent(), SuspendableEvent, CharacterContext<C> {
    var approach = false
    val operate: Boolean
        get() = !approach
    override var onCancel: (() -> Unit)? = null
    var launched = false

    abstract fun copy(approach: Boolean): Interaction<C>
}