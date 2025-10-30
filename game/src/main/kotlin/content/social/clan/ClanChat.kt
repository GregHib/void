package content.social.clan

import content.social.friend.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.*
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.client.instruction.ClanChatJoin
import world.gregs.voidps.network.client.instruction.ClanChatKick
import world.gregs.voidps.network.client.instruction.ClanChatRank
import world.gregs.voidps.network.login.protocol.encode.appendClanChat
import world.gregs.voidps.network.login.protocol.encode.updateClanChat
import java.util.concurrent.TimeUnit

class ClanChat : Script {

    val accounts: AccountDefinitions by inject()
    val maxMembers = 100
    val maxAttempts = 10
    val players: Players by inject()

    val list = listOf(ClanRank.None, ClanRank.Recruit, ClanRank.Corporeal, ClanRank.Sergeant, ClanRank.Lieutenant, ClanRank.Captain, ClanRank.General)

    val accountDefinitions: AccountDefinitions by inject()

    init {
        playerSpawn { player ->
            val current = player["clan_chat", ""]
            if (current.isNotEmpty()) {
                val account = accountDefinitions.getByAccount(current)
                joinClan(player, account?.displayName ?: "")
            }
            val ownClan = accounts.clan(player.name.lowercase()) ?: return@playerSpawn
            player.ownClan = ownClan
            ownClan.friends = player.friends
            ownClan.ignores = player.ignores
        }

        playerDespawn { player ->
            val clan = player.clan ?: return@playerDespawn
            clan.members.remove(player)
            updateMembers(player, clan, ClanRank.Anyone)
        }

        instruction<ClanChatKick> { player ->
            val clan = player.clan
            if (clan == null || !clan.hasRank(player, clan.kickRank)) {
                player.message("You are not allowed to kick in this clan chat channel.", ChatType.ClanChat)
                return@instruction
            }

            if (player.name == name) {
                player.message("You cannot kick or ban yourself.", ChatType.ClanChat)
                return@instruction
            }

            val target = players.get(name)
            if (target == null) {
                player.message("Could not find player with the username '$name'.")
                return@instruction
            }

            if (!clan.hasRank(player, clan.getRank(target), inclusive = false) || target.isAdmin()) {
                player.message("You cannot kick this member.", ChatType.ClanChat)
                return@instruction
            }

            if (clan.members.contains(target)) {
                target.emit(LeaveClanChat(forced = true))
            }
            player.message("Your request to kick/ban this user was successful.", ChatType.ClanChat)
        }

        instruction<ClanChatJoin> { player ->
            if (name.isBlank()) {
                player.emit(LeaveClanChat(forced = false))
                return@instruction
            }
            joinClan(player, name)
        }

        instruction<ClanChatRank> { player ->
            val clan = player.clan ?: player.ownClan ?: return@instruction
            if (!clan.hasRank(player, ClanRank.Owner)) {
                return@instruction
            }
            val rank = list[rank]
            val account = accountDefinitions.get(name) ?: return@instruction
            if (player.friends[account.accountName] == rank) {
                return@instruction
            }
            player.friends[account.accountName] = rank
            player.updateFriend(account)
            if (clan.members.any { it.accountName == account.accountName }) {
                val target = players.get(name) ?: return@instruction
                updateMembers(target, clan, rank)
            }
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
                    victim.emit(LeaveClanChat(forced = true))
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

    fun display(player: Player, clan: Clan) {
        player.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, clan.members.map { ClanMember.of(it, clan.getRank(it)) })
        player.message("Now talking in clan channel ${clan.name}", ChatType.ClanChat)
        player.message("To talk, start each line of chat with the / symbol.", ChatType.ClanChat)
        updateMembers(player, clan)
    }

    fun updateMembers(player: Player, clan: Clan, rank: ClanRank = clan.getRank(player)) {
        for (member in clan.members) {
            member.client?.appendClanChat(ClanMember.of(player, rank))
        }
    }

    fun joinClan(player: Player, name: String) {
        if (player.remaining("clan_join_spam", epochSeconds()) > 0) {
            player.message("You are temporarily blocked from joining channels - please try again later!", ChatType.ClanChat)
            return
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
            return
        } else if (clan == null || clan.name.isEmpty()) {
            player.message("The channel you tried to join does not exist.", ChatType.ClanChat)
            return
        }

        if (player.clan == clan) {
            display(player, clan)
        } else {
            join(player, clan)
        }
    }
}
