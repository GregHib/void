package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.updateFriend
import world.gregs.voidps.engine.data.definition.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.extra.AccountDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.engine.entity.character.player.chat.friend.AddFriend
import world.gregs.voidps.engine.entity.character.player.chat.friend.DeleteFriend
import world.gregs.voidps.engine.entity.character.player.chat.ignore.AddIgnore
import world.gregs.voidps.engine.entity.character.player.chat.ignore.DeleteIgnore
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.network.encode.sendFriendsList
import world.gregs.voidps.world.community.chat.privateStatus
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.community.ignore.ignores

val players: Players by inject()
val accounts: AccountDefinitions by inject()

val maxFriends = 200

on<Registered> { player: Player ->
    player.sendFriends()
    notifyBefriends(player, online = true)
}

on<Unregistered> { player: Player ->
    notifyBefriends(player, online = false)
}

on<AddFriend> { player: Player ->
    val account = accounts.get(friend)
    if (account == null) {
        player.message("Unable to find player with name '$friend'.")
        cancel()
        return@on
    }

    if (player.name == friend) {
        player.message("You are already your own best friend!")
        cancel()
        return@on
    }

    if (player.ignores.contains(account.accountName)) {
        player.message("Please remove $friend from your ignore list first.")
        cancel()
        return@on
    }

    if (player.friends.size >= maxFriends) {
        player.message("Your friends list is full. Max of 100 for free users, and $maxFriends for members.")
        cancel()
        return@on
    }

    if (player.friends.contains(account.accountName)) {
        player.message("$friend is already on your friends list.")
        cancel()
        return@on
    }

    player.friends[account.accountName] = ClanRank.Friend
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = true)
    }
    player.sendFriend(account)
}

on<DeleteFriend> { player: Player ->
    val account = accounts.get(friend)
    if (account == null || !player.friends.contains(account.accountName)) {
        player.message("Unable to find player with name '$friend'.")
        cancel()
        return@on
    }

    player.friends.remove(account.accountName)
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = false)
    }
}

on<AddIgnore>(priority = Priority.LOWER) { player: Player ->
    val other = players.get(name)
    if (other != null && other.friend(player) && !other.isAdmin()) {
        other.updateFriend(Friend(player.name, player.previousName, world = 0))
    }
}

on<DeleteIgnore>({ player -> player.privateStatus == "on" }, Priority.LOWER) { player: Player ->
    val other = players.get(name)
    if (other != null && (other.friend(player) || other.isAdmin())) {
        other.updateFriend(Friend(player.name, player.previousName, world = World.id, worldName = World.name))
    }
}

on<InterfaceOption>({ id == "filter_buttons" && component == "private" && it.privateStatus != "on" && option != "Off" }, Priority.HIGH) { player: Player ->
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

on<InterfaceOption>({ id == "filter_buttons" && component == "private" && it.privateStatus != "off" && option != "On" }, Priority.HIGH) { player: Player ->
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

on<LeaveClanChat>(priority = Priority.HIGH) { player: Player ->
    val clan: Clan = player.clan ?: return@on
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
    return Friend(account.displayName, account.previousName, rank, world = if (online) World.id else 0, worldName = World.name)
}

fun Player.visibleOnline(friend: Player): Boolean {
    return privateStatus == "on" && !ignores(friend) || privateStatus == "friends" && friend(friend)
}

fun notifyBefriends(player: Player, online: Boolean, notify: (Player, String) -> Boolean = friends(player)) {
    players
        .filter { it.friend(player) && notify(it, player.privateStatus) }
        .forEach { friend ->
            friend.updateFriend(Friend(player.name, player.previousName, world = if (online) World.id else 0, worldName = World.name))
        }
}

fun String.updateFriend(friend: Player, online: Boolean) {
    players.get(this)?.updateFriend(Friend(friend.name, friend.previousName, world = if (online) World.id else 0, worldName = World.name))
}