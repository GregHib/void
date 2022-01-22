package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.*
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.network.encode.Member
import world.gregs.voidps.network.encode.appendClanChat
import world.gregs.voidps.network.encode.leaveClanChat
import world.gregs.voidps.network.encode.updateClanChat
import java.util.concurrent.TimeUnit

val accounts: AccountDefinitions by inject()
val maxMembers = 100
val maxAttempts = 10
val players: Players by inject()
val banTicks = TimeUnit.HOURS.toTicks(1)

on<Registered> { player: Player ->
    val current = player["clan_chat", ""]
    if (current.isNotEmpty()) {
        val account = accountDefinitions.getByAccount(current)
        player.events.emit(JoinClanChat(account?.displayName ?: ""))
    }
}

on<Unregistered>({ it.contains("clan") }) { player: Player ->
    val clan: Clan = player["clan"]
    clan.members.remove(player)
    updateMembers(player, clan, Rank.Anyone)
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

    if (target.hasEffect("clan_ban_${clan.name.toUnderscoreCase()}")) {
        target.start("clan_ban_${clan.name.toUnderscoreCase()}", banTicks, quiet = true)
        player.message("Your request to kick/ban this user was successful.", ChatType.ClanChat)
        return@on
    }

    if (!clan.hasRank(player, clan.getRank(target), inclusive = false) || target.isAdmin()) {
        player.message("You cannot kick this member.", ChatType.ClanChat)
        return@on
    }

    target.start("clan_ban_${clan.name.toUnderscoreCase()}", banTicks)
    if (clan.members.contains(target)) {
        target.events.emit(LeaveClanChat(kick = true))
    }
    player.message("Your request to kick/ban this user was successful.", ChatType.ClanChat)
}

on<JoinClanChat> { player: Player ->
    if (player.hasEffect("clan_chat_spam")) {
        player.message("You are temporarily blocked from joining channels - please try again later!", ChatType.ClanChat)
        return@on
    }
    if (player.hasOrStart("join_clan_attempt", TimeUnit.MINUTES.toTicks(1))) {
        val attempts = player["clan_join_attempts", 0] + 1
        player["clan_join_attempts"] = attempts
        if (attempts > maxAttempts) {
            player.start("clan_join_spamming", TimeUnit.MINUTES.toTicks(5))
        }
    }

    player.message("Attempting to join channel...", ChatType.ClanChat)
    val clan = accounts.clan(name)
    if (clan != null && clan.owner == player.accountName && clan.name.isEmpty()) {
        clan.name = player.name
        player["clan_name", true] = name
        player.message("Your friends chat channel has now been enabled!", ChatType.ClanChat)
        player.message("Join your channel by clicking 'Join Chat' and typing: ${player.name}", ChatType.ClanChat)
        return@on
    } else if (clan == null || clan.name.isEmpty()) {
        player.message("The channel you tried to join does not exist.", ChatType.ClanChat)
        return@on
    }

    if (player.getOrNull<Clan>("clan") == clan) {
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

    if (!player.isAdmin() && player.hasEffect("clan_ban_${clan.owner.toUnderscoreCase()}")) {
        player.message("You do not have a high enough rank to join this clan chat channel.", ChatType.ClanChat)
        return
    }

    if (clan.members.size >= maxMembers) {
        var space = false
        if (clan.hasRank(player, Rank.Recruit)) {
            val victim = clan.members.minByOrNull { clan.getRank(it).value }
            if (victim != null) {
                victim.events.emit(LeaveClanChat(kick = true))
                space = true
            }
        }

        if (!space) {
            player.message("The channel you tried to join is currently full.", ChatType.ClanChat)
            return
        }
    }

    player["clan"] = clan
    player["clan_chat", true] = clan.owner
    clan.members.add(player)
    display(player, clan)
}

on<LeaveClanChat> { player: Player ->
    val clan: Clan? = player.remove("clan")
    player.clear("clan_chat")
    player.message("You have ${if (kick) "been kicked from" else "left"} the channel.", ChatType.ClanChat)
    if (clan != null) {
        player.client?.leaveClanChat()
        clan.members.remove(player)
        updateMembers(player, clan, Rank.Anyone)
    }
}

fun display(player: Player, clan: Clan) {
    player.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, clan.members.map { toMember(it, clan.getRank(it)) })
    player.message("Now talking in friends chat channel ${clan.name}", ChatType.ClanChat)
    player.message("To talk, start each line of chat with the / symbol.", ChatType.ClanChat)
    updateMembers(player, clan)
}

fun updateMembers(player: Player, clan: Clan, rank: Rank = clan.getRank(player)) {
    for (member in clan.members) {
        if (member != player) {
            member.client?.appendClanChat(toMember(player, rank))
        }
    }
}

fun toMember(player: Player, rank: Rank) = Member(
    player.name,
    World.id,
    rank.value,
    World.name
)

val list = listOf(Rank.None, Rank.Recruit, Rank.Corporeal, Rank.Sergeant, Rank.Lieutenant, Rank.Captain, Rank.General)

val accountDefinitions: AccountDefinitions by inject()

on<UpdateClanChatRank> { player: Player ->
    val clan = player.clan ?: return@on
    val account = accountDefinitions.get(name) ?: return@on
    val rank = list[rank]
    player.friends[account.accountName] = rank
    if (clan.members.any { it.accountName == account.accountName }) {
        val target = players.get(name) ?: return@on
        updateMembers(target, clan, rank)
    }
}