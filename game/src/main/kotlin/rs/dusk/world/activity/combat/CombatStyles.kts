package rs.dusk.world.activity.combat

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.player.display.InterfaceInteraction

IntVariable(43, Variable.Type.VARP, true, 0).register("combat_style")

InterfaceOpened where { name == "combat_styles" } then {
    var unlock = true
    for (index in 11..14) {
        player.interfaces.sendSettings(id, index, -1, 0, if (unlock) 2 else 0)
    }
}

InterfaceInteraction where { name == "combat_styles" } then {
    when (componentId) {
        in 11..14 -> player.setVar("combat_style", componentId - 11)//Attack style
    }
}