package world.gregs.voidps.engine.entity.character.player.chat.friend

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class DeleteFriend(val friend: String) : CancellableEvent() {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "delete_friend"
        else -> null
    }
}

fun friendsDelete(block: DeleteFriend.(Player) -> Unit) {
    Events.handle("delete_friend", handler = block)
}