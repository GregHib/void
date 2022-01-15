package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name

fun Player.friend(other: Player) = this != other && friends.contains(other.name)

fun Player.friend(other: String) = name != other && friends.contains(other)