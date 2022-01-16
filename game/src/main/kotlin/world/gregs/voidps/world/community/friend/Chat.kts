import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.PrivateChat
import world.gregs.voidps.engine.entity.character.player.chat.PrivateChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.PublicChat
import world.gregs.voidps.engine.entity.character.player.chat.PublicChatMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.privateChatFrom
import world.gregs.voidps.network.encode.privateChatTo
import world.gregs.voidps.network.encode.publicChat

val players: Players by inject()

on<PublicChat> { player: Player ->
    val message = PublicChatMessage(player, effects, text)
    player.viewport.players.current.forEach {
        it.events.emit(message)
    }
}

on<PublicChatMessage>({ it.client != null }) { player: Player ->
    player.client?.publicChat(source.index, effects, source.rights.ordinal, compressed)
}

on<PrivateChat> { player: Player ->
    val target = players.get(friend)
    if (target == null) {
        player.message("Unable to send message - player unavailable.")
        return@on
    }
    val message = PrivateChatMessage(player, message)
    player.client?.privateChatTo(target.name, message.compressed)
    target.events.emit(message)
}

on<PrivateChatMessage>({ it.client != null }) { player: Player ->
    player.client?.privateChatFrom(source.name, source.rights.ordinal, compressed)
}