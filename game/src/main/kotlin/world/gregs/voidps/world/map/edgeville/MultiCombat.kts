package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.variableClear
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player

enterArea(tag = "multi_combat") {
    player["in_multi_combat"] = true
}

exitArea(tag = "multi_combat") {
    player.clear("in_multi_combat")
}

variableSet("in_multi_combat", true) { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

variableClear("in_multi_combat") { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}