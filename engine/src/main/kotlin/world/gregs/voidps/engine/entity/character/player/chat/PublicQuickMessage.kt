package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.event.Event

data class PublicQuickMessage(
    val script: Int,
    val file: Int,
    val data: ByteArray
) : Event