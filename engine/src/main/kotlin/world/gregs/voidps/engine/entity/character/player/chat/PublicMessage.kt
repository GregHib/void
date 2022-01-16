package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

data class PublicMessage(
    val message: String,
    val effects: Int
) : Event