package rs.dusk.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceOption

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
    val equipOption = itemDef.params?.get(527L + optionId) as? String
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