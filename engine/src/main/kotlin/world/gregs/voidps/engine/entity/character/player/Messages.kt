package world.gregs.voidps.engine.entity.character.player

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message

fun Player.cantReach() = message("You can't reach that.")

fun Player.inventoryFull() = message("You don't have enough inventory space.")

fun Player.noInterest() = message("Nothing interesting happens.")

fun Player.notEnough(thing: String) = message("You don't have enough ${thing.toTitleCase()}.")