package rs.dusk.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceInteraction

on(InterfaceInteraction) {
    where {
        name == "worn_equipment" && component == "bonuses" && option == "Show Equipment Stats"
    }
    then {
        player.open("equipment_bonuses")
    }
}

on(InterfaceInteraction) {
    where {
        name == "worn_equipment" && component == "price" && option == "Show Price-checker"
    }
    then {
        player.open("price_checker")
    }
}

on(InterfaceInteraction) {
    where {
        name == "worn_equipment" && component == "items" && option == "Show Items Kept on Death"
    }
    then {
        player.open("items_kept_on_death")
    }
}

val decoder: ItemDecoder by inject()
val bus: EventBus by inject()
val logger = InlineLogger()

InterfaceInteraction where { name == "worn_equipment" && option == "*" } then {
    val itemDef = decoder.get(itemId)
    var equipOption = itemDef.params?.get(526L + optionId) as? String ?: itemDef.options.getOrNull(optionId)
    if(equipOption == null) {
        logger.info { "Unhandled equipment option $itemId - $optionId" }
        return@then
    }
    if(equipOption == "Wield" || equipOption == "Wear") {
        equipOption = "Remove"
    }
    val slot = EquipSlot.by(component)
    bus.emit(ContainerAction(player, name, item, slot.index, equipOption))
}

InterfaceInteraction where { name == "inventory" && component == "container" && optionId == 8 } then {
    bus.emit(ContainerAction(player, name, item, itemIndex, "Examine"))
}