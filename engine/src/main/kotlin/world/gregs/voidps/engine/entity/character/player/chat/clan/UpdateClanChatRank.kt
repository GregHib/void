package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class UpdateClanChatRank(val name: String, val rank: Int) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "update_clan_chat_rank"
        else -> null
    }
}