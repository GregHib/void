package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject

val logger = InlineLogger()
val decoder: ItemDefinitions by inject()

on<InterfaceRefreshed>({ name == "inventory" }) { player: Player ->
    player.interfaceOptions.unlockAll(name, "container", 0 until 28)
    player.interfaceOptions.unlock(name, "container", 28 until 56, "Drag")
    player.sendContainer(name)
}

on<InterfaceSwitch>({ name == "inventory" && toName == "inventory" }) { player: Player ->
    val container = player.inventory
    var fromItem = decoder.getName(fromItemId)
    if (fromItem.isBlank()) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@on
        }

        fromItem = container.getItemId(fromSlot)
    }
    if (!container.isValidId(fromSlot, fromItem)) {
        logger.debug { "Interface $id component $componentId from item $fromItem slot $fromSlot not found for player $player" }
        return@on
    }

    val toItem = decoder.getName(toItemId)
    val toSlot = toSlot - 28
    if (!container.isValidId(toSlot, toItem)) {
        logger.debug { "Interface $toId component $toComponentId to item $toItem slot $toSlot not found for player $player" }
        return@on
    }
    player.inventory.swap(fromSlot, toSlot)
}

on<InterfaceOption>({ name == "inventory" && component == "container" }) { player: Player ->
    val itemDef = item.def
    val equipOption = when (optionId) {
        6 -> itemDef.options.getOrNull(3)
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionId)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $item $optionId" }
        return@on
    }
    player.events.emit(
        ContainerOption(
            name,
            item,
            itemIndex,
            equipOption
        )
    )
}