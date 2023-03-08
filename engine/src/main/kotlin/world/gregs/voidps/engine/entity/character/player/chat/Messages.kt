package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.cantReach() = message("You can't reach that.")

fun Player.cantReachI() = message("I can't reach that!", ChatType.Engine)

fun Player.inventoryFull() = notEnough("inventory space")

fun Player.noInterest() = message("Nothing interesting happens.", ChatType.Engine)

fun Player.notEnough(thing: String) = message("You don't have enough ${thing}.")