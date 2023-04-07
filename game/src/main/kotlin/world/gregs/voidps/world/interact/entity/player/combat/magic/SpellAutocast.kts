package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.clear
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.getComponentOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.attackRange

val interfaceDefinitions: InterfaceDefinitions by inject()

on<InterfaceOption>({ id.endsWith("_spellbook") && option == "Autocast" }) { player: Player ->
    val value: Int? = interfaceDefinitions.get(id).getComponentOrNull(component)?.getOrNull("cast_id")
    if (value == null || player.get<Int>("autocast") == value) {
        player.clear("autocast")
    } else {
        player["autocast_spell"] = component
        player.attackRange = 8
        player["autocast"] = value
    }
}

on<VariableSet>({ key == "autocast" && to == null }) { player: Player ->
    player.clear("autocast_spell")
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index }) { player: Player ->
    player.clear("autocast")
}