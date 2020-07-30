package rs.dusk.engine.model.entity.character.player.chat

import rs.dusk.engine.client.send
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ChatMessage

fun Player.message(text: String, type: ChatType = ChatType.Game, tile: Int = 0, name: String? = null) {
    send(ChatMessage(type.id, tile, text, name, name?.toLowerCase()?.replace(" ", "_")))
}