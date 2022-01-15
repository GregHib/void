import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.updateFriend
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.AddFriend
import world.gregs.voidps.engine.entity.character.player.chat.DeleteFriend
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.player.previousName
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.network.encode.sendFriendsList
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.community.friend.privateStatus
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
    if (player.ignores(friend)) {
        cancel()
        return@on
    }

    if (player.friends.size >= maxFriends) {
        cancel()
        player.message("Your friends list is full. Max of 100 for free users, and $maxFriends for members.")
        return@on
    }

    if (player.friends.contains(friend)) {
        cancel()
        return@on
    }

    player.friends.add(friend)
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = true)
    }
    player.sendFriend(friend)
}

on<DeleteFriend> { player: Player ->
    if (!player.friends.contains(friend)) {
        cancel()
        player.message("Unable to find player with name '$friend'.")
        return@on
    }
    player.friends.remove(friend)
    if (player.privateStatus == "friends") {
        friend.updateFriend(player, online = false)
    }
}

on<InterfaceOption>({ id == "filter_buttons" && component == "private" && it.privateStatus != "on" && option != "Off" }, Priority.HIGH) { player: Player ->
    val next = option.lowercase()
    notifyBefriends(player, online = true) { it, current ->
        println("$current $next")
        when {
            current == "off" && next == "on" -> !it.isAdmin()
            current == "off" && next == "friends" -> friends(player, it)
            current == "friends" && next == "on" -> !friends(player, it)
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

fun friends(player: Player) = { other: Player, status: String ->
    when (status) {
        "friends" -> friends(player, other)
        "off" -> other.isAdmin()
        "on" -> true
        else -> false
    }
}

fun friends(player: Player, it: Player) = player.friend(it) || it.isAdmin()


fun Player.sendFriends() {
    client?.sendFriendsList(friends.mapNotNull { it.toFriend(this) })
}

fun Player.sendFriend(friend: String) {
    val player = friend.toFriend(this)
    if (player == null) {
        message("Unable to find player with name '$friend'.")
        return
    }
    client?.sendFriendsList(listOf(player))
}

fun String.toFriend(player: Player): Friend? {
    val friend = players.get(this)
    val display = friend?.name ?: accounts.get(this)?.displayName ?: return null
    val previous = friend?.previousName ?: accounts.get(this)?.previousName ?: return null
    val rank = 0
    val online = friend != null && friend.visibleOnline(player)
    return Friend(display, previous, rank, online = online)
}

fun Player.visibleOnline(friend: Player): Boolean {
    return privateStatus == "on" && !ignores(friend) || privateStatus == "friends" && friend(friend)
}

fun notifyBefriends(player: Player, online: Boolean, notify: (Player, String) -> Boolean = friends(player)) {
    players.indexed
        .filterNotNull()
        .filter { it.friend(player) && notify(it, player.privateStatus) }
        .forEach { friend ->
            friend.updateFriend(Friend(player.name, player.previousName, online = online))
        }
}

fun String.updateFriend(friend: Player, online: Boolean) {
    players.get(this)?.updateFriend(Friend(friend.name, friend.previousName, online = online))
}