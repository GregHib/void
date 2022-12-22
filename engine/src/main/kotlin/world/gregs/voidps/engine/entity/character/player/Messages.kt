package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.message

fun Player.cantReach() = message("You can't reach that.")

fun Player.inventoryFull() = notEnough("inventory space")

fun Player.noInterest() = message("Nothing interesting happens.")

fun Player.notEnough(thing: String) = message("You don't have enough ${thing}.")