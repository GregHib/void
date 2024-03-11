package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class LeaveClanChat(val forced: Boolean) : Event {
    override val notification = false

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "leave_clan_chat"
        1 -> true // prioritise non-overrides
        else -> null
    }
}

fun clanChatLeave(override: Boolean = true, block: LeaveClanChat.(Player) -> Unit) {
    Events.handle("leave_clan_chat", if (override) "*" else true, override = override, handler = block)
}