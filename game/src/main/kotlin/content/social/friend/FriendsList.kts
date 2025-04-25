package content.social.friend

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.updateFriend
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.chat.clan.clanChatLeave
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import content.social.chat.privateStatus
import content.social.clan.ClanLootShare
import content.social.clan.ClanMember
import content.social.clan.clan
import content.social.clan.ownClan
import content.social.ignore.ignores
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.network.client.instruction.FriendAdd
import world.gregs.voidps.network.client.instruction.FriendDelete
import world.gregs.voidps.network.login.protocol.encode.*

val players: Players by inject()
val accounts: AccountDefinitions by inject()
val accountDefinitions: AccountDefinitions by inject()

val maxFriends = 200

playerSpawn { player ->
    player.sendFriends()
    notifyBefriends(player, online = true)
}

playerDespawn { player ->
    notifyBefriends(player, online = false)
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
    player.sendFriend(account)
    val clan = player.clan ?: player.ownClan ?: return@instruction
    if (!clan.hasRank(player, ClanRank.Owner)) {
        return@instruction
    }
    val accountDefinition = accountDefinitions.get(friendsName) ?: return@instruction
    if (clan.members.any { it.accountName == accountDefinition.accountName }) {
        val target = players.get(friendsName) ?: return@instruction
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
        val target = players.get(friendsName) ?: return@instruction
        for (member in clan.members) {
            member.client?.appendClanChat(ClanMember.of(target, ClanRank.None))
        }
        if (!clan.hasRank(target, clan.joinRank)) {
            target.emit(LeaveClanChat(forced = true))
        }
    }
}

interfaceOption(component = "private", id = "filter_buttons") {
    if (player.privateStatus != "on" && option != "Off") {
        val next = option.lowercase()
        notifyBefriends(player, online = true) { it, current ->
            when {
                current == "off" && next == "on" -> !player.ignores(it)
                current == "off" && next == "friends" -> !it.isAdmin() && friends(player, it)
                current == "friends" && next == "on" -> !friends(player, it) && !player.ignores(it)
                else -> false
            }
        }
    } else if (player.privateStatus != "off" && option != "On") {
        val next = option.lowercase()
        notifyBefriends(player, online = false) { it, current ->
            when {
                current == "friends" && next == "off" -> player.friend(it) && !it.isAdmin()
                current == "on" && next == "friends" -> !friends(player, it)
                current == "on" && next == "off" -> !it.isAdmin()
                else -> false
            }
        }
    } else {
        return@interfaceOption
    }
}

clanChatLeave { player ->
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

fun friends(player: Player) = { other: Player, status: String ->
    when (status) {
        "friends" -> friends(player, other)
        "off" -> other.isAdmin()
        "on" -> !player.ignores(other)
        else -> false
    }
}

fun friends(player: Player, it: Player) = player.friend(it) || it.isAdmin()

fun Player.sendFriends() {
    client?.sendFriendsList(friends.mapNotNull { toFriend(this, accounts.getByAccount(it.key) ?: return@mapNotNull null) })
}

fun Player.sendFriend(friend: AccountDefinition) {
    client?.sendFriendsList(listOf(toFriend(this, friend)))
}

fun toFriend(player: Player, account: AccountDefinition): Friend {
    val friend = players.get(account.displayName)
    val rank = 0
    val online = friend != null && (player.isAdmin() || friend.visibleOnline(player))
    return Friend(account.displayName, account.previousName, rank, world = if (online) Settings.world else 0, worldName = Settings.worldName)
}

fun Player.visibleOnline(friend: Player): Boolean {
    return privateStatus == "on" && !ignores(friend) || privateStatus == "friends" && friend(friend)
}

fun notifyBefriends(player: Player, online: Boolean, notify: (Player, String) -> Boolean = friends(player)) {
    players
        .filter { it.friend(player) && notify(it, player.privateStatus) }
        .forEach { friend ->
            friend.updateFriend(Friend(player.name, player.previousName, world = if (online) Settings.world else 0, worldName = Settings.worldName))
        }
}

fun String.updateFriend(friend: Player, online: Boolean) {
    players.get(this)?.updateFriend(Friend(friend.name, friend.previousName, world = if (online) Settings.world else 0, worldName = Settings.worldName))
}