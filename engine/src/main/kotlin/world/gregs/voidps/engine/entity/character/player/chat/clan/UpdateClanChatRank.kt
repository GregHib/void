package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.event.Event

data class UpdateClanChatRank(val name: String, val rank: Int) : Event