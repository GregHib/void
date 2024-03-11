package world.gregs.voidps.engine.entity.character.player.chat.ignore

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class AddIgnore(val name: String) : CancellableEvent() {
    override val notification = false

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "add_ignore"
        1 -> true // prioritise non-overrides
        else -> null
    }
}

fun ignoresAdd(override: Boolean = true, block: AddIgnore.(Player) -> Unit) {
    Events.handle("add_ignore", if (override) "*" else true, override = override, handler = block)
}