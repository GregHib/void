package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.mode.move.characterEnterArea
import world.gregs.voidps.engine.entity.character.mode.move.characterExitArea

characterEnterArea(tag = "multi_combat") {
    character["in_multi_combat"] = true
}

characterExitArea(tag = "multi_combat") {
    character.clear("in_multi_combat")
}

variableSet("in_multi_combat", to = true) { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

variableSet("in_multi_combat", to = null) { player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}