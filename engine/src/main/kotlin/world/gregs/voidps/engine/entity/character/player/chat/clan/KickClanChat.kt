package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class KickClanChat(val name: String) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "kick_clan_chat"
        else -> null
    }
}