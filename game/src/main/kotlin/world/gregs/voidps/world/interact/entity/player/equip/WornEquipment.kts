package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.event.on


on<InterfaceOpened>({ name == "worn_equipment" }) { player: Player ->
    player.sendContainer(name)
}

on<InterfaceOption>({ name == "worn_equipment" && component == "bonuses" && option == "Show Equipment Stats" }) { player: Player ->
//    player.setVar("equipment_banking", false)
    player.open("equipment_bonuses")
}

on<InterfaceOption>({ name == "worn_equipment" && component == "price" && option == "Show Price-checker" }) { player: Player ->
    player.open("price_checker")
}

on<InterfaceOption>({ name == "worn_equipment" && component == "items" && option == "Show Items Kept on Death" }) { player: Player ->
    player.open("items_kept_on_death")
}

val logger = InlineLogger()

on<InterfaceOption>({ name == "worn_equipment" && option == "*" }) { player: Player ->
    val equipOption = getEquipmentOption(item.def, optionId)
    if (equipOption == null) {
        logger.info { "Unhandled equipment option $item - $optionId" }
        return@on
    }
    val slot = EquipSlot.by(component)
    player.events.emit(ContainerOption(name, item, slot.index, equipOption))
}

fun getEquipmentOption(itemDef: ItemDefinition, optionId: Int): String? {
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