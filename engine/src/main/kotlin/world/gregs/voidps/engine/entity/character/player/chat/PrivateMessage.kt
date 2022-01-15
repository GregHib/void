package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

data class PrivateMessage(
    val friend: String,
    val message: String
) : Event