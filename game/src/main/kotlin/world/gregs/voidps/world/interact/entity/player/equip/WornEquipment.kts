package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.network.visual.update.player.EquipSlot


on<InterfaceRefreshed>({ id == "worn_equipment" }) { player: Player ->
    player.sendInventory(id)
}

on<InterfaceOption>({ id == "worn_equipment" && component == "bonuses" && option == "Show Equipment Stats" }) { player: Player ->
//    player.setVar("equipment_banking", false)
    player.open("equipment_bonuses")
}

on<InterfaceOption>({ id == "worn_equipment" && component == "price" && option == "Show Price-checker" }) { player: Player ->
    player.open("price_checker")
}

on<InterfaceOption>({ id == "worn_equipment" && component == "items" && option == "Show Items Kept on Death" }) { player: Player ->
    player.open("items_kept_on_death")
}

val logger = InlineLogger()

on<InterfaceOption>({ id == "worn_equipment" && option == "*" }) { player: Player ->
    val equipOption = getEquipmentOption(item.def, optionIndex)
    if (equipOption == null) {
        logger.info { "Unhandled equipment option $item - $optionIndex" }
        return@on
    }
    val slot = EquipSlot.by(component)
    player.closeInterfaces()
    player.events.emit(InventoryOption(player, id, item, slot.index, equipOption))
}

fun getEquipmentOption(itemDef: ItemDefinition, optionId: Int): String? {
    val equipOption: String? = itemDef.getOrNull<Map<Int, String>>("worn_options")?.get(optionId - 1)
    if (equipOption != null) {
        return equipOption
    }
    return when (optionId) {
        0 -> "Remove"
        5 -> itemDef.getOrNull("extra_equipment_option")
        9 -> "Examine"
        else -> null
    }
}