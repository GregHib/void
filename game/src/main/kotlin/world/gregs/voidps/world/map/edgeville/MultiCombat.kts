import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.utility.inject

val areas: Areas by inject()

val multiAreas = areas.getTagged("multi_combat")

fun inMultiCombat(tile: Tile) = multiAreas.any { tile in it.area }

on<Registered>({ inMultiCombat(it.tile) }, Priority.LOW) { player: Player ->
    player.start("in_multi_combat")
}

on<Moved>({ !inMultiCombat(from) && inMultiCombat(to) }) { player: Player ->
    player.start("in_multi_combat")
}

on<Moved>({ inMultiCombat(from) && !inMultiCombat(to) }) { player: Player ->
    player.stop("in_multi_combat")
}

on<EffectStart>({ effect == "in_multi_combat" }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

on<EffectStop>({ effect == "in_multi_combat" }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}