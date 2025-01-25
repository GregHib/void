package content.social.friend

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.friend(other: Player) = this != other && friends.contains(other.accountName)

val Settings.world: Int
    get() = this["world.id", 16]

val Settings.worldName: String
    get() = this["world.name", "World 16"]