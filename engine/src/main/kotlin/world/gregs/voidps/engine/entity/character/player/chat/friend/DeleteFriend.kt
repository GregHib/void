package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class DeleteFriend(val friend: String) : CancellableEvent() {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "delete_friend"
        1 -> true // prioritise non-overrides
        else -> null
    }
}

fun friendsDelete(override: Boolean = true, block: DeleteFriend.(Player) -> Unit) {
    Events.handle("delete_friend", if (override) "*" else true, override = override, handler = block)
}