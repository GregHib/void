package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority

enterArea({ tags.contains("multi_combat") }, Priority.LOW) { player: Player ->
    player["in_multi_combat"] = true
}

exitArea({ tags.contains("multi_combat") }, Priority.LOW) { player: Player ->
    player.clear("in_multi_combat")
}

variableSet({ key == "in_multi_combat" && to == true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

variableSet({ key == "in_multi_combat" && to != true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}