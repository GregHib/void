package world.gregs.voidps.world.community.ignore

import world.gregs.voidps.engine.entity.character.player.Player

fun Player.ignores(other: Player): Boolean = this != other && ignores.contains(other.accountName)
