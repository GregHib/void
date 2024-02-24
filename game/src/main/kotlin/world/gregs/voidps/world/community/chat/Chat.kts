package world.gregs.voidps.world.community.chat

import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.friend.PrivateChat
import world.gregs.voidps.engine.entity.character.player.chat.friend.PrivateChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicChat
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicChatMessage
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.emit
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.encode.clanChat
import world.gregs.voidps.network.encode.privateChatFrom
import world.gregs.voidps.network.encode.privateChatTo
import world.gregs.voidps.network.encode.publicChat
import world.gregs.voidps.world.community.clan.chatType
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.community.ignore.ignores

val players: Players by inject()
val huffman: Huffman by inject()

on<PublicChat>({ it.chatType == "public" }) { player ->
    val message = PublicChatMessage(player, effects, text, huffman)
    players.filter { it.tile.within(player.tile, VIEW_RADIUS) && !it.ignores(player) }.forEach {
        it.emit(message)
    }
}

on<PublicChatMessage>({ it.networked }) { player ->
    player.client?.publicChat(source.index, effects, source.rights.ordinal, compressed)
}

on<PrivateChat> { player ->
    val target = players.get(friend)
    if (target == null || target.ignores(player)) {
        player.message("Unable to send message - player unavailable.")
        return@on
    }
    val message = PrivateChatMessage(player, message, huffman)
    player.client?.privateChatTo(target.name, message.compressed)
    target.emit(message)
}

on<PrivateChatMessage>({ it.networked }) { player ->
    player.client?.privateChatFrom(source.name, source.rights.ordinal, compressed)
}

on<PublicChat>({ it.chatType == "clan" }) { player ->
    val clan = player.clan
    if (clan == null) {
        player.message("You must be in a clan chat to talk.", ChatType.ClanChat)
        return@on
    }
    if (!clan.hasRank(player, clan.talkRank) || !clan.members.contains(player)) {
        player.message("You are not allowed to talk in this clan chat.", ChatType.ClanChat)
        return@on
    }
    val message = ClanChatMessage(player, effects, text, huffman)
    clan.members.filterNot { it.ignores(player) }.forEach {
        it.emit(message)
    }
}

on<ClanChatMessage>({ it.networked }) { player ->
    player.client?.clanChat(source.name, player.clan!!.name, source.rights.ordinal, compressed)
}