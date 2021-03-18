package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.utility.inject


InterfaceOpened where { name == "worn_equipment" } then {
    player.sendContainer(name)
}

InterfaceOption where { name == "worn_equipment" && component == "bonuses" && option == "Show Equipment Stats" } then {
//    player.setVar("equipment_banking", false)
    player.open("equipment_bonuses")
}

InterfaceOption where { name == "worn_equipment" && component == "price" && option == "Show Price-checker" } then {
    player.open("price_checker")
}

InterfaceOption where { name == "worn_equipment" && component == "items" && option == "Show Items Kept on Death" } then {
    player.open("items_kept_on_death")
}

val decoder: ItemDefinitions by inject()
val bus: EventBus by inject()
val logger = InlineLogger()

InterfaceOption where { name == "worn_equipment" && option == "*" } then {
    val equipOption = getEquipmentOption(itemId, optionId)
    if (equipOption == null) {
        logger.info { "Unhandled equipment option $itemId - $optionId" }
        return@then
    }
    val slot = EquipSlot.by(component)
    bus.emit(ContainerAction(player, name, item, slot.index, equipOption))
}

fun getEquipmentOption(itemId: Int, optionId: Int): String? {
    val itemDef = decoder.get(itemId)
    val equipOption: String? = itemDef.getParamOrNull(527L + optionId)
    if (equipOption != null) {
        return equipOption
    }
    return when (optionId) {
        0 -> "Remove"
        9 -> "Examine"
        else -> null
    }
}

InterfaceOption where { name == "inventory" && component == "container" && optionId == 9 } then {
    bus.emit(ContainerAction(player, name, item, itemIndex, "Examine"))
}