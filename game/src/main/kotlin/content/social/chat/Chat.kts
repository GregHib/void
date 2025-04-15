package content.social.chat

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.friend.PrivateChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicChatMessage
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.clanChat
import world.gregs.voidps.network.login.protocol.encode.privateChatFrom
import world.gregs.voidps.network.login.protocol.encode.privateChatTo
import world.gregs.voidps.network.login.protocol.encode.publicChat
import content.social.clan.chatType
import content.social.clan.clan
import content.social.ignore.ignores
import world.gregs.voidps.engine.client.instruction.onInstruction
import world.gregs.voidps.network.client.instruction.ChatPrivate
import world.gregs.voidps.network.client.instruction.ChatPublic
import world.gregs.voidps.network.client.instruction.ChatTypeChange

val players: Players by inject()
val huffman: Huffman by inject()

onEvent<Player, PublicChatMessage> { player ->
    player.client?.publicChat(source.index, effects, source.rights.ordinal, compressed)
}

onInstruction<ChatPrivate> { player ->
    val target = players.get(friend)
    if (target == null || target.ignores(player)) {
        player.message("Unable to send message - player unavailable.")
        return@onInstruction
    }
    val message = PrivateChatMessage(player, message, huffman)
    player.client?.privateChatTo(target.name, message.compressed)
    target.emit(message)
}

onEvent<Player, PrivateChatMessage> { player ->
    player.client?.privateChatFrom(source.name, source.rights.ordinal, compressed)
}

onInstruction<ChatTypeChange> { player ->
    player["chat_type"] = when (type) {
        1 -> "clan"
        else -> "public"
    }
}

onInstruction<ChatPublic> { player ->
    when (player.chatType) {
        "public" -> {
            val message = PublicChatMessage(player, effects, text, huffman)
            players.filter { it.tile.within(player.tile, VIEW_RADIUS) && !it.ignores(player) }.forEach {
                it.emit(message)
            }
        }
        "clan" -> {
            val clan = player.clan
            if (clan == null) {
                player.message("You must be in a clan chat to talk.", ChatType.ClanChat)
                return@onInstruction
            }
            if (!clan.hasRank(player, clan.talkRank) || !clan.members.contains(player)) {
                player.message("You are not allowed to talk in this clan chat.", ChatType.ClanChat)
                return@onInstruction
            }
            val message = ClanChatMessage(player, effects, text, huffman)
            clan.members.filterNot { it.ignores(player) }.forEach {
                it.emit(message)
            }
        }
    }
}

onEvent<Player, ClanChatMessage> { player ->
    player.client?.clanChat(source.name, player.clan!!.name, source.rights.ordinal, compressed)
}