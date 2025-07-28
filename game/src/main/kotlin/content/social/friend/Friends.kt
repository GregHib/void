package content.social.friend

import content.social.chat.privateStatus
import content.social.ignore.ignores
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.Friend
import world.gregs.voidps.network.login.protocol.encode.sendFriendsList

fun Player.friend(other: Player) = this != other && friends.contains(other.accountName)

val Settings.world: Int
    get() = this["world.id", 16]

val Settings.worldName: String
    get() = this["world.name", "World 16"]


fun Player.updateFriend(friend: Friend) = client?.sendFriendsList(listOf(friend)) ?: Unit

fun Player.updateFriend(friend: AccountDefinition) {
    client?.sendFriendsList(listOf(toFriend(this, friend)))
}

fun toFriend(player: Player, account: AccountDefinition): Friend {
    val friend = get<Players>().get(account.displayName)
    val rank = player.friends[account.accountName] ?: ClanRank.Anyone
    val online = friend != null && (player.isAdmin() || friend.visibleOnline(player))
    return Friend(account.displayName, account.previousName, rank.value, world = if (online) Settings.world else 0, worldName = Settings.worldName)
}

private fun Player.visibleOnline(friend: Player): Boolean = privateStatus == "on" && !ignores(friend) || privateStatus == "friends" && friend(friend)
