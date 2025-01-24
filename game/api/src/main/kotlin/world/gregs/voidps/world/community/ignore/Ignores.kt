package world.gregs.voidps.world.community.ignore

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin

fun Player.ignores(other: Player): Boolean = this != other && !other.isAdmin() && ignores.contains(other.accountName)
