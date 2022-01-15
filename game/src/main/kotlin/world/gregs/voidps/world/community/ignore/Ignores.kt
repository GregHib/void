package world.gregs.voidps.world.community.ignore

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.name

fun Player.ignores(other: String): Boolean = name != other && ignores.contains(other)

fun Player.ignores(other: Player): Boolean = ignores(other.name)