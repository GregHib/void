package rs.dusk.world.activity.combat

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

IntVariable(43, Variable.Type.VARP, true, 0).register("attack_style")
NegativeBooleanVariable(172, Variable.Type.VARP, true).register("auto_retaliate")

InterfaceOpened where { name == "combat_styles" } then {
    player.interfaceOptions.unlockAll(name, "style1")
    player.interfaceOptions.unlockAll(name, "style2")
    player.interfaceOptions.unlockAll(name, "style3")
    player.interfaceOptions.unlockAll(name, "style4")
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