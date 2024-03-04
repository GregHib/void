package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea

enterArea(tag = "multi_combat") {
    player["in_multi_combat"] = true
}

exitArea(tag = "multi_combat") {
    player.clear("in_multi_combat")
}

variableSet("in_multi_combat", to = true) { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

variableSet("in_multi_combat", to = null) { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}