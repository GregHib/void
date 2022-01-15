import world.gregs.voidps.engine.client.message
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
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.network.encode.sendFriendsList
import world.gregs.voidps.world.community.friend.friend
import world.gregs.voidps.world.community.friend.status
import world.gregs.voidps.world.community.ignore.ignores

val players: Players by inject()
val accounts: AccountDefinitions by inject()

val maxFriends = 200

on<Registered> { player: Player ->
    player.sendFriends()
    update(player)
}

on<Unregistered> { player: Player ->
    update(player, true)
}

on<AddFriend> { player: Player ->
    if (player.ignores(friend)) {
        return@on
    }

    if (player.friends.size >= maxFriends) {
        player.message("Your friends list is full. Max of 100 for free users, and $maxFriends for members.")
        return@on
    }

    if (player.friends.contains(friend)) {
        return@on
    }

    player.friends.add(friend)
    notifyFriends(player)
    player.sendFriend(friend)
    promote(player, friend, 0)
}

on<DeleteFriend> { player: Player ->
    if (!player.friends.contains(friend)) {
        player.message("Unable to find player with name '$friend'.")
        return@on
    }
    player.friends.remove(friend)
    notifyFriends(player)
    demote(player, friend)
}

fun notifyFriends(player: Player) {
    players.forEach {
        if (it.friend(player) && player.status == 1) {
            it.sendFriend(player)
        }
    }
}

fun update(player: Player, logout: Boolean = false) {
    players.indexed
        .filter { it != null && it.friend(player) && ((!player.ignores(it) && player.statusOnline(it)) || it.isAdmin()) }
        .forEach { other ->
            println("Send ${player.name} logout to ${other?.name}")
            other?.sendFriend(player, logout)
        }
}


fun Player.statusOnline(friend: Player): Boolean {
    return status == 0 && !ignores(friend) || status == 1 && friend(friend)
}

fun demote(player: Player, friend: String) {

}

fun promote(player: Player, friend: String, rank: Int) {

}

fun Player.sendFriends() {
    client?.sendFriendsList(friends.mapNotNull { account ->
        val (display, previous) = accounts.get(account) ?: return@mapNotNull null
        val rank = 0//players.get(account)?
        Friend(display, previous, rank, false, 0)
    })
}

fun Player.sendFriend(friend: String) {
    val friendAccount = players.get(friend)
    if (friendAccount != null) {
        sendFriend(friendAccount)
    } else {
        val account = accounts.get(friend)
        if (account == null) {
            message("Unable to find player with name '$friend'.")
            return
        }
        val (display, previous) = account
        client?.sendFriendsList(listOf(Friend(display, previous, 0, false, 0)))
    }
}

fun Player.sendFriend(friend: Player, logout: Boolean = false) {
    client?.sendFriendsList(listOf(Friend(friend.name, friend.previousName, 0, false, if (!logout && friend.statusOnline(this)) 1 else 0, "World 1")))
}