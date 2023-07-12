package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.character.mode.move.AreaEntered
import world.gregs.voidps.engine.entity.character.mode.move.AreaExited
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

on<AreaEntered>({ tags.contains("multi_combat") }, Priority.LOW) { player: Player ->
    player["in_multi_combat"] = true
}

on<AreaExited>({ tags.contains("multi_combat") }, Priority.LOW) { player: Player ->
    player.clear("in_multi_combat")
}

on<VariableSet>({ key == "in_multi_combat" && to == true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

on<VariableSet>({ key == "in_multi_combat" && to != true }) { player: Player ->
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
}