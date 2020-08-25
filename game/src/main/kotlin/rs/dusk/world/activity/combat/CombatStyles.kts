package rs.dusk.world.activity.combat

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

IntVariable(43, Variable.Type.VARP, true, 0).register("attack_style")
NegativeBooleanVariable(172, Variable.Type.VARP, true).register("auto_retaliate")

InterfaceOpened where { name == "combat_styles" } then {
    var unlock = true
    val settings = if (unlock) intArrayOf(0) else intArrayOf()
    player.interfaces.sendSettings(name, "style1", -1, 0, *settings)
    player.interfaces.sendSettings(name, "style2", -1, 0, *settings)
    player.interfaces.sendSettings(name, "style3", -1, 0, *settings)
    player.interfaces.sendSettings(name, "style4", -1, 0, *settings)
    player.sendVar("attack_style")
    player.sendVar("auto_retaliate")
}

InterfaceOption where { name == "combat_styles" && component.startsWith("style") } then {
    val index = component.replace("style", "").toIntOrNull() ?: return@then
    player.setVar("attack_style", index - 1)
}

InterfaceOption where { name == "combat_styles" && component == "retaliate" && option == "Auto Retaliate" } then {
    player.toggleVar("auto_retaliate")
}