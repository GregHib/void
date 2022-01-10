package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.attackRange
import world.gregs.voidps.world.interact.entity.combat.weapon

val interfaceDefinitions: InterfaceDefinitions by inject()

on<InterfaceOption>({ id.endsWith("_spellbook") && option == "Autocast" }) { player: Player ->
    val value: Int? = interfaceDefinitions.get(id).getComponentOrNull(component)?.getOrNull("cast_id")
    if (value == null || player.getVar<Int>("autocast") == value) {
        player.clearVar("autocast")
    } else {
        player["autocast"] = component
        player.attackRange = 8
        player.setVar("autocast", value)
    }
}

on<VariableSet>({ key == "autocast" && to == 0 }) { player: Player ->
    player.clear("autocast")
    player.attackRange = player.weapon.def["attack_range", 1]
}