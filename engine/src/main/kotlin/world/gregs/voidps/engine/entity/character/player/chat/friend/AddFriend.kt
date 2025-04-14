package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class AddFriend(val friend: String) : CancellableEvent() {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "add_friend"
        1 -> true // prioritise non-overrides
        else -> null
    }
}

fun friendsAdd(block: AddFriend.(Player) -> Unit) {
    Events.handle("add_friend", "*", override = true, handler = block)
}