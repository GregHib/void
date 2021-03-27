package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.getProperty

on<Registered> { player: Player ->
    player.message("Welcome to $name.")
}

val name = getProperty("name")