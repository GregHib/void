import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.clear
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas

val areas: Areas by inject()

val multiAreas = areas.getTagged("multi_combat")
val singleAreas = areas.getTagged("single_combat")

fun inMultiCombat(tile: Tile) = multiAreas.any { tile in it.area } && singleAreas.none { tile in it.area }

on<Registered>({ inMultiCombat(it.tile) }, Priority.LOW) { player: Player ->
    player["in_multi_combat"] = true
}

on<Moved>({ !inMultiCombat(from) && inMultiCombat(to) }) { player: Player ->
    player["in_multi_combat"] = true
}

on<Moved>({ inMultiCombat(from) && !inMultiCombat(to) }) { player: Player ->
    player.clear("in_multi_combat")
}

on<VariableSet>({ key == "in_multi_combat" && to == true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

on<VariableSet>({ key == "in_multi_combat" && to != true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}