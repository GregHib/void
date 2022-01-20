package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event

/**
 * A quick-chat message sent in a clan chat from [source].
 */
data class ClanQuickChatMessage(
    val source: Player,
    val script: Int,
    val file: Int,
    val message: String,
    val data: ByteArray
) : Event