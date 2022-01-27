package world.gregs.voidps.world.community.friend

import world.gregs.voidps.engine.entity.character.player.Player

fun Player.friend(other: Player) = this != other && friends.contains(other.accountName)