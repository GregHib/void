package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.*
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.clanChat
import world.gregs.voidps.network.encode.privateChatFrom
import world.gregs.voidps.network.encode.privateChatTo
import world.gregs.voidps.network.encode.publicChat
import world.gregs.voidps.world.community.clan.chatType
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.community.ignore.ignores

val players: Players by inject()

on<PublicChat>({ it.chatType == "public" }) { player: Player ->
    val message = PublicChatMessage(player, effects, text)
    player.viewport.players.filterNot { it.ignores(player) }.forEach {
        it.events.emit(message)
    }
}

on<PublicChatMessage>({ it.client != null }) { player: Player ->
    player.client?.publicChat(source.index, effects, source.rights.ordinal, compressed)
}

on<PrivateChat> { player: Player ->
    val target = players.get(friend)
    if (target == null || target.ignores(player)) {
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

on<PublicChat>({ it.chatType == "clan" }) { player: Player ->
    val clan = player.clan
    if (clan == null) {
        player.message("You must be in a clan chat to talk.", ChatType.ClanChat)
        return@on
    }
    if (!clan.hasRank(player, clan.talkRank) || !clan.members.contains(player)) {
        player.message("You are not allowed to talk in this clan chat.", ChatType.ClanChat)
        return@on
    }
    val message = ClanChatMessage(player, effects, text)
    clan.members.filterNot { it.ignores(player) }.forEach {
        it.events.emit(message)
    }
}

on<ClanChatMessage>({ it.client != null }) { player: Player ->
    player.client?.clanChat(source.name, player.clan!!.name, source.rights.ordinal, compressed)
}