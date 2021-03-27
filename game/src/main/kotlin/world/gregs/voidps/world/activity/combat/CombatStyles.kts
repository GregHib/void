package world.gregs.voidps.world.activity.combat

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

IntVariable(43, Variable.Type.VARP, true, 0).register("attack_style")
NegativeBooleanVariable(172, Variable.Type.VARP, true).register("auto_retaliate")

on<InterfaceOpened>({ name == "combat_styles" }) { player: Player ->
    player.interfaceOptions.unlockAll(name, "style1")
    player.interfaceOptions.unlockAll(name, "style2")
    player.interfaceOptions.unlockAll(name, "style3")
    player.interfaceOptions.unlockAll(name, "style4")
    player.sendVar("attack_style")
    player.sendVar("auto_retaliate")
}

on<InterfaceOption>({ name == "combat_styles" && component.startsWith("style") }) { player: Player ->
    val index = component.replace("style", "").toIntOrNull() ?: return@on
    player.setVar("attack_style", index - 1)
}

on<InterfaceOption>({ name == "combat_styles" && component == "retaliate" && option == "Auto Retaliate" }) { player: Player ->
    player.toggleVar("auto_retaliate")
}