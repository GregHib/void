import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.utility.inject

val areas: Areas by inject()

val wilderness = areas.getValue("wilderness").area
val stealingCreation = areas.getValue("stealing_creation_lobby").area
val clanWars = areas.getValue("clan_wars_lobby").area
val bountyHunter = areas.getValue("bounty_hunter_lobby").area

fun inWilderness(tile: Tile) = tile in wilderness && tile !in stealingCreation && tile !in clanWars && tile !in bountyHunter

on<Registered>({ inWilderness(it.tile) }) { player: Player ->
    player.start("in_wilderness")
}

on<Moved>({ !inWilderness(from) && inWilderness(to) }) { player: Player ->
    player.start("in_wilderness")
}

on<Moved>({ inWilderness(from) && !inWilderness(to) }) { player: Player ->
    player.stop("in_wilderness")
}

on<EffectStart>({ effect == "in_wilderness" }) { player: Player ->
    player.options.set(1, "Attack")
    player.open("wilderness_skull")
}

on<EffectStop>({ effect == "in_wilderness" }) { player: Player ->
    player.options.remove("Attack")
    player.close("wilderness_skull")
}