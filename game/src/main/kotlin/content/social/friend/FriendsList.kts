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
import world.gregs.voidps.engine.entity.character.player.chat.friend.friendsAdd
import world.gregs.voidps.engine.entity.character.player.chat.friend.friendsDelete
import world.gregs.voidps.engine.entity.character.player.chat.ignore.ignoresAdd
import world.gregs.voidps.engine.entity.character.player.chat.ignore.ignoresDelete
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.Friend
import world.gregs.voidps.network.login.protocol.encode.sendFriendsList
import content.social.chat.privateStatus
import content.social.clan.clan
import content.social.ignore.ignores

val players: Players by inject()
val accounts: AccountDefinitions by inject()

val maxFriends = 200

playerSpawn { player ->
    player.sendFriends()
    notifyBefriends(player, online = true)
}

playerDespawn { player ->
    notifyBefriends(player, online = false)
}

friendsAdd { player ->
    val account = accounts.get(friend)
    if (account == null) {
        player.message("Unable to find player with name '$friend'.")
        cancel()
        return@friendsAdd
    }

    if (player.name == friend) {
        player.message("You are already your own best friend!")
        cancel()
        return@friendsAdd
    }

    if (player.ignores.contains(account.accountName)) {
        player.message("Please remove $friend from your ignore list first.")
        cancel()
        return@friendsAdd
    }

    if (player.friends.size >= maxFriends) {
        player.message("Your friends list is full. Max of 100 for free users, and $maxFriends for members.")
        cancel()
        return@friendsAdd
    }

    if (player.friends.contains(account.accountName)) {
        player.message("$friend is already on your friends list.")
        cancel()
        return@friendsAdd
    }

    player.friends[account.accountName] = ClanRank.Friend
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = true)
    }
    player.sendFriend(account)
}

friendsDelete(override = false) { player ->
    val account = accounts.get(friend)
    if (account == null || !player.friends.contains(account.accountName)) {
        player.message("Unable to find player with name '$friend'.")
        cancel()
        return@friendsDelete
    }

    player.friends.remove(account.accountName)
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = false)
    }
}

ignoresAdd { player ->
    val other = players.get(name)
    if (other != null && other.friend(player) && !other.isAdmin()) {
        other.updateFriend(Friend(player.name, player.previousName, world = 0))
    }
}

ignoresDelete { player ->
    if(player.privateStatus != "on") {
        return@ignoresDelete
    }
    val other = players.get(name)
    if (other != null && (other.friend(player) || other.isAdmin())) {
        other.updateFriend(Friend(player.name, player.previousName, world = Settings.world, worldName = Settings.worldName))
    }
}

interfaceOption(component = "private", id = "filter_buttons") {
    if (player.privateStatus == "on" || option == "Off") {
        return@interfaceOption
    }
    val next = option.lowercase()
    notifyBefriends(player, online = true) { it, current ->
        when {
            current == "off" && next == "on" -> !player.ignores(it)
            current == "off" && next == "friends" -> !it.isAdmin() && friends(player, it)
            current == "friends" && next == "on" -> !friends(player, it) && !player.ignores(it)
            else -> false
        }
    }
}

interfaceOption(component = "private", id = "filter_buttons") {
    if (player.privateStatus == "off" || option == "On") {
        return@interfaceOption
    }
    val next = option.lowercase()
    notifyBefriends(player, online = false) { it, current ->
        when {
            current == "friends" && next == "off" -> player.friend(it) && !it.isAdmin()
            current == "on" && next == "friends" -> !friends(player, it)
            current == "on" && next == "off" -> !it.isAdmin()
            else -> false
        }
    }
}

clanChatLeave { player ->
    val clan: Clan = player.clan ?: return@clanChatLeave
    if (player.accountName != clan.owner || player.isAdmin()) {
        player.sendFriends()
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