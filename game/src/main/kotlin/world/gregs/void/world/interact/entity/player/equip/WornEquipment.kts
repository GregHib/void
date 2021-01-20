package world.gregs.void.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.client.variable.setVar
import world.gregs.void.engine.entity.definition.ItemDefinitions
import world.gregs.void.engine.entity.item.EquipSlot
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.on
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

InterfaceOption where {  name == "worn_equipment" && component == "bonuses" && option == "Show Equipment Stats" } then {
    player.setVar("equipment_banking", false)
    player.open("equipment_bonuses")
}

on(InterfaceOption) {
    where {
        name == "worn_equipment" && component == "price" && option == "Show Price-checker"
    }
    then {
        player.open("price_checker")
    }
}

on(InterfaceOption) {
    where {
        name == "worn_equipment" && component == "items" && option == "Show Items Kept on Death"
    }
    then {
        player.open("items_kept_on_death")
    }
}

val decoder: ItemDefinitions by inject()
val bus: EventBus by inject()
val logger = InlineLogger()

InterfaceOption where { name == "worn_equipment" && option == "*" } then {
    val equipOption = getEquipmentOption(itemId, optionId)
    if(equipOption == null) {
        logger.info { "Unhandled equipment option $itemId - $optionId" }
        return@then
    }
    val slot = EquipSlot.by(component)
    bus.emit(ContainerAction(player, name, item, slot.index, equipOption))
}

fun getEquipmentOption(itemId: Int, optionId: Int): String? {
    val itemDef = decoder.get(itemId)
    val equipOption: String? = itemDef.getParam(527L + optionId)
    if(equipOption != null) {
        return equipOption
    }
    return when(optionId) {
        0 -> "Remove"
        9 -> "Examine"
        else -> null
    }
}

InterfaceOption where { name == "inventory" && component == "container" && optionId == 9 } then {
    bus.emit(ContainerAction(player, name, item, itemIndex, "Examine"))
}