package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class LeaveClanChat(val forced: Boolean) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "leave_clan_chat"
        else -> null
    }
}

fun clanChatLeave( block: LeaveClanChat.(Player) -> Unit) {
    Events.handle("leave_clan_chat", handler = block)
}