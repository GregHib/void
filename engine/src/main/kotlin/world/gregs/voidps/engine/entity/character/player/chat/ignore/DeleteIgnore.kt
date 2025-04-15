package world.gregs.voidps.engine.entity.character.player.chat.ignore

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class DeleteIgnore(var name: String) : CancellableEvent() {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "delete_ignore"
        else -> null
    }
}

fun ignoresDelete(block: DeleteIgnore.(Player) -> Unit) {
    Events.handle("delete_ignore", handler = block)
}