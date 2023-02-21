import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop

val areas: Areas by inject()

val multiAreas = areas.getTagged("multi_combat")
val singleAreas = areas.getTagged("single_combat")

fun inMultiCombat(tile: Tile) = multiAreas.any { tile in it.area } && singleAreas.none { tile in it.area }

on<Registered>({ inMultiCombat(it.tile) }, Priority.LOW) { player: Player ->
    player.softTimers.start("in_multi_combat")
}

on<Moved>({ !inMultiCombat(from) && inMultiCombat(to) }) { player: Player ->
    player.softTimers.start("in_multi_combat")
}

on<Moved>({ inMultiCombat(from) && !inMultiCombat(to) }) { player: Player ->
    player.softTimers.stop("in_multi_combat")
}

on<TimerStart>({ timer == "in_multi_combat" }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

on<TimerStop>({ timer == "in_multi_combat" }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}