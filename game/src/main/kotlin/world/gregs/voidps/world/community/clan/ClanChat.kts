package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.*
import world.gregs.voidps.engine.entity.character.player.chat.friend.AddFriend
import world.gregs.voidps.engine.entity.character.player.chat.friend.DeleteFriend
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.encode.Member
import world.gregs.voidps.network.encode.appendClanChat
import world.gregs.voidps.network.encode.leaveClanChat
import world.gregs.voidps.network.encode.updateClanChat
import java.util.concurrent.TimeUnit

val accounts: AccountDefinitions by inject()
val maxMembers = 100
val maxAttempts = 10
val players: Players by inject()

on<Registered> { player: Player ->
    val current = player["clan_chat", ""]
    if (current.isNotEmpty()) {
        val account = accountDefinitions.getByAccount(current)
        player.events.emit(JoinClanChat(account?.displayName ?: ""))
    }
    val ownClan = accounts.clan(player.name) ?: return@on
    player.ownClan = ownClan
    ownClan.friends = player.friends
    ownClan.ignores = player.ignores
}

on<Unregistered>({ player -> player.clan != null }) { player: Player ->
    val clan = player.clan ?: return@on
    clan.members.remove(player)
    updateMembers(player, clan, ClanRank.Anyone)
}

on<KickClanChat> { player: Player ->
    val clan = player.clan
    if (clan == null || !clan.hasRank(player, clan.kickRank)) {
        player.message("You are not allowed to kick in this clan chat channel.", ChatType.ClanChat)
        return@on
    }

    if (player.name == name) {
        player.message("You cannot kick or ban yourself.", ChatType.ClanChat)
        return@on
    }

    val target = players.get(name)
    if (target == null) {
        player.message("Could not find player with the username '$name'.")
        return@on
    }

    if (!clan.hasRank(player, clan.getRank(target), inclusive = false) || target.isAdmin()) {
        player.message("You cannot kick this member.", ChatType.ClanChat)
        return@on
    }

    if (clan.members.contains(target)) {
        target.events.emit(LeaveClanChat(forced = true))
    }
    player.message("Your request to kick/ban this user was successful.", ChatType.ClanChat)
}

on<JoinClanChat> { player: Player ->
    if (player.remaining("clan_join_spam", epochSeconds()) > 0) {
        player.message("You are temporarily blocked from joining channels - please try again later!", ChatType.ClanChat)
        return@on
    }
    if (player.hasClock("join_clan_attempt")) {
        val attempts = player["clan_join_attempts", 0] + 1
        player["clan_join_attempts"] = attempts
        if (attempts > maxAttempts) {
            player.start("clan_join_spam", TimeUnit.MINUTES.toSeconds(5).toInt(), epochSeconds())
        }
    } else {
        player.start("join_clan_attempt", TimeUnit.MINUTES.toTicks(1))
    }

    player.message("Attempting to join channel...", ChatType.ClanChat)
    val clan = accounts.clan(name)
    if (clan != null && clan.owner == player.accountName && clan.name.isEmpty()) {
        clan.name = player.name
        player["clan_name"] = name
        player.message("Your clan chat channel has now been enabled!", ChatType.ClanChat)
        player.message("Join your channel by clicking 'Join Chat' and typing: ${player.name}", ChatType.ClanChat)
        return@on
    } else if (clan == null || clan.name.isEmpty()) {
        player.message("The channel you tried to join does not exist.", ChatType.ClanChat)
        return@on
    }

    if (player.clan == clan) {
        display(player, clan)
    } else {
        join(player, clan)
    }
}

fun join(player: Player, clan: Clan) {
    if (player.contains("clan")) {
        player.message("You are already in a clan chat channel.")
        return
    }

    if (!clan.hasRank(player, clan.joinRank)) {
        player.message("You do not have a high enough rank to join this clan chat channel.", ChatType.ClanChat)
        return
    }

    if (!player.isAdmin() && clan.ignores.contains(player.accountName)) {
        player.message("You are banned from joining this clan chat channel.", ChatType.ClanChat)
        return
    }

    if (clan.members.size >= maxMembers) {
        var space = false
        if (clan.hasRank(player, ClanRank.Recruit)) {
            val victim = clan.members.minByOrNull { clan.getRank(it).value }
            if (victim != null) {
                victim.events.emit(LeaveClanChat(forced = true))
                space = true
            }
        }

        if (!space) {
            player.message("The channel you tried to join is currently full.", ChatType.ClanChat)
            return
        }
    }

    player.clan = clan
    player["clan_chat"] = clan.owner
    clan.members.add(player)
    display(player, clan)
}

on<LeaveClanChat> { player: Player ->
    val clan: Clan? = player.remove("clan")
    player.clear("clan_chat")
    player.message("You have ${if (forced) "been kicked from" else "left"} the channel.", ChatType.ClanChat)
    if (clan != null) {
        player.client?.leaveClanChat()
        clan.members.remove(player)
        updateMembers(player, clan, ClanRank.Anyone)
    }
}

fun display(player: Player, clan: Clan) {
    player.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, clan.members.map { toMember(it, clan.getRank(it)) })
    player.message("Now talking in clan channel ${clan.name}", ChatType.ClanChat)
    player.message("To talk, start each line of chat with the / symbol.", ChatType.ClanChat)
    updateMembers(player, clan)
}

fun updateMembers(player: Player, clan: Clan, rank: ClanRank = clan.getRank(player)) {
    for (member in clan.members) {
        if (member != player) {
            member.client?.appendClanChat(toMember(player, rank))
        }
    }
}

fun toMember(player: Player, rank: ClanRank) = Member(
    player.name,
    World.id,
    rank.value,
    World.name
)

val list = listOf(ClanRank.None, ClanRank.Recruit, ClanRank.Corporeal, ClanRank.Sergeant, ClanRank.Lieutenant, ClanRank.Captain, ClanRank.General)

val accountDefinitions: AccountDefinitions by inject()

on<UpdateClanChatRank> { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        return@on
    }
    val rank = list[rank]
    val account = accountDefinitions.get(name) ?: return@on
    player.friends[account.accountName] = rank
    if (clan.members.any { it.accountName == account.accountName }) {
        val target = players.get(name) ?: return@on
        updateMembers(target, clan, rank)
    }
}

on<AddFriend>(priority = Priority.LOWER) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        return@on
    }
    val account = accountDefinitions.get(friend) ?: return@on
    if (clan.members.any { it.accountName == account.accountName }) {
        val target = players.get(friend) ?: return@on
        for (member in clan.members) {
            member.client?.appendClanChat(toMember(target, ClanRank.Friend))
        }
    }
}

on<DeleteFriend>(priority = Priority.LOWER) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        return@on
    }
    val account = accountDefinitions.get(friend) ?: return@on
    if (clan.members.any { it.accountName == account.accountName }) {
        val target = players.get(friend) ?: return@on
        for (member in clan.members) {
            member.client?.appendClanChat(toMember(target, ClanRank.None))
        }
        if (!clan.hasRank(target, clan.joinRank)) {
            target.events.emit(LeaveClanChat(forced = true))
        }
    }
}
