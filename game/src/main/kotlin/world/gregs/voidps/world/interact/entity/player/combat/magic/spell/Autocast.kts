package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.attackRange

val interfaceDefinitions: InterfaceDefinitions by inject()

interfaceOption("*_spellbook", option = "Autocast") {
    val value: Int? = interfaceDefinitions.getComponent(id, component)?.getOrNull("cast_id")
    if (value == null || player["autocast", 0] == value) {
        player.clear("autocast")
    } else {
        player["autocast_spell"] = component
        player.attackRange = 8
        player["autocast"] = value
    }
}

variableSet({ key == "autocast" && to == null }) { player: Player ->
    player.clear("autocast_spell")
}

itemChanged({ inventory == "worn_equipment" && index == EquipSlot.Weapon.index }) { player: Player ->
    player.clear("autocast")
}