package content.social.friend

import content.social.chat.privateStatus
import content.social.clan.ClanLootShare
import content.social.clan.ClanMember
import content.social.clan.clan
import content.social.clan.ownClan
import content.social.ignore.ignores
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.FriendAdd
import world.gregs.voidps.network.client.instruction.FriendDelete
import world.gregs.voidps.network.login.protocol.encode.*

class FriendsList(
    val accounts: AccountDefinitions,
    val accountDefinitions: AccountDefinitions,
) : Script {

    val maxFriends = 200

    init {
        playerSpawn {
            sendFriends()
            notifyBefriends(this, online = true)
        }

        playerDespawn {
            notifyBefriends(this, online = false)
        }

        instruction<FriendAdd> { player ->
            val account = accounts.get(friendsName)
            if (account == null) {
                player.message("Unable to find player with name '$friendsName'.")
                return@instruction
            }

            if (player.name == friendsName) {
                player.message("You are already your own best friend!")
                return@instruction
            }

            if (player.ignores.contains(account.accountName)) {
                player.message("Please remove $friendsName from your ignore list first.")
                return@instruction
            }

            if (player.friends.size >= maxFriends) {
                player.message("Your friends list is full. Max of 100 for free users, and $maxFriends for members.")
                return@instruction
            }

            if (player.friends.contains(account.accountName)) {
                player.message("$friendsName is already on your friends list.")
                return@instruction
            }

            player.friends[account.accountName] = ClanRank.Friend
            if (player.privateStatus == "friends") {
                friendsName.updateFriend(player, online = true)
            }
            player.updateFriend(account)
            val clan = player.clan ?: player.ownClan ?: return@instruction
            if (!clan.hasRank(player, ClanRank.Owner)) {
                return@instruction
            }
            val accountDefinition = accountDefinitions.get(friendsName) ?: return@instruction
            if (clan.members.any { it.accountName == accountDefinition.accountName }) {
                val target = Players.get(friendsName) ?: return@instruction
                for (member in clan.members) {
                    member.client?.appendClanChat(ClanMember.of(target, ClanRank.Friend))
                }
            }
        }

        instruction<FriendDelete> { player ->
            val account = accounts.get(friendsName)
            if (account == null || !player.friends.contains(account.accountName)) {
                player.message("Unable to find player with name '$friendsName'.")
                return@instruction
            }

            player.friends.remove(account.accountName)
            if (player.privateStatus == "friends") {
                friendsName.updateFriend(player, online = false)
            }
            val clan = player.clan ?: player.ownClan ?: return@instruction
            if (!clan.hasRank(player, ClanRank.Owner)) {
                return@instruction
            }
            val accountDefinition = accountDefinitions.get(friendsName) ?: return@instruction
            if (clan.members.any { it.accountName == accountDefinition.accountName }) {
                val target = Players.get(friendsName) ?: return@instruction
                for (member in clan.members) {
                    member.client?.appendClanChat(ClanMember.of(target, ClanRank.None))
                }
                if (!clan.hasRank(target, clan.joinRank)) {
                    leaveClan(target, true)
                }
            }
        }

        interfaceOption(id = "filter_buttons:private") {
            val option = it.option
            if (privateStatus != "on" && option != "Off") {
                val next = option.lowercase()
                notifyBefriends(this, online = true) { p, current ->
                    when {
                        current == "off" && next == "on" -> !ignores(p)
                        current == "off" && next == "friends" -> !p.isAdmin() && friends(this, p)
                        current == "friends" && next == "on" -> !friends(this, p) && !ignores(p)
                        else -> false
                    }
                }
            } else if (privateStatus != "off" && option != "On") {
                val next = option.lowercase()
                notifyBefriends(this, online = false) { p, current ->
                    when {
                        current == "friends" && next == "off" -> friend(p) && !p.isAdmin()
                        current == "on" && next == "friends" -> !friends(this, p)
                        current == "on" && next == "off" -> !p.isAdmin()
                        else -> false
                    }
                }
            }
            privateStatus = option.lowercase()
        }
    }

    companion object {
        val leaveClan: (Player, Boolean) -> Unit = { player, forced ->
            val clan: Clan? = player.remove("clan")
            player.clear("clan_chat")
            player.message("You have ${if (forced) "been kicked from" else "left"} the channel.", ChatType.ClanChat)
            if (clan != null) {
                player.client?.leaveClanChat()
                clan.members.remove(player)
                for (member in clan.members) {
                    if (member != player) {
                        member.client?.appendClanChat(ClanMember.of(player, ClanRank.Anyone))
                    }
                }
                if (player.accountName != clan.owner || player.isAdmin()) {
                    player.sendFriends()
                }
                ClanLootShare.update(player, clan, lootShare = false)
            }
        }

        private fun Player.sendFriends() {
            client?.sendFriendsList(friends.mapNotNull { toFriend(this, get<AccountDefinitions>().getByAccount(it.key) ?: return@mapNotNull null) })
        }
    }

    fun friends(player: Player) = { other: Player, status: String ->
        when (status) {
            "friends" -> friends(player, other)
            "off" -> other.isAdmin()
            "on" -> !player.ignores(other)
            else -> false
        }
    }

    fun friends(player: Player, it: Player) = player.friend(it) || it.isAdmin()

    fun notifyBefriends(player: Player, online: Boolean, notify: (Player, String) -> Boolean = friends(player)) {
        Players
            .filter { it.friend(player) && notify(it, player.privateStatus) }
            .forEach { friend ->
                friend.updateFriend(
                    Friend(
                        name = player.name,
                        previousName = player.previousName,
                        rank = (friend.friends[player.accountName] ?: ClanRank.Friend).value,
                        world = if (online) Settings.world else 0,
                        worldName = Settings.worldName,
                    ),
                )
            }
    }

    fun String.updateFriend(friend: Player, online: Boolean) {
        val player = Players.get(this) ?: return
        player.updateFriend(
            Friend(
                name = friend.name,
                previousName = friend.previousName,
                rank = (player.friends[friend.accountName] ?: ClanRank.Friend).value,
                world = if (online) Settings.world else 0,
                worldName = Settings.worldName,
            ),
        )
    }
}
