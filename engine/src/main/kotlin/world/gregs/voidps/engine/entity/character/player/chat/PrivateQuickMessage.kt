package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

data class PrivateQuickMessage(
    val friend: String,
    val file: Int,
    val data: ByteArray
) : Event