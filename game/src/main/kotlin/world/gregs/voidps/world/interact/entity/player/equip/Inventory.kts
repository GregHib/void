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
import world.gregs.voidps.engine.utility.inject

val logger = InlineLogger()
val decoder: ItemDefinitions by inject()

on<InterfaceRefreshed>({ id == "inventory" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "container", 0 until 28)
    player.interfaceOptions.unlock(id, "container", 28 until 56, "Drag")
    player.sendContainer(id)
}

on<InterfaceSwitch>({ id == "inventory" && toId == "inventory" }) { player: Player ->
    val container = player.inventory
    var fromItem = decoder.get(fromItemId).stringId
    if (fromItem.isBlank()) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponent from slot $fromSlot not found for player $player" }
            return@on
        }

        fromItem = container.getItemId(fromSlot)
    }
    if (!container.isValidId(fromSlot, fromItem)) {
        logger.debug { "Interface $id component $component from item $fromItem slot $fromSlot not found for player $player" }
        return@on
    }

    val toItem = decoder.get(toItemId).stringId
    val toSlot = toSlot - 28
    if (!container.isValidId(toSlot, toItem)) {
        logger.debug { "Interface $toId component $toComponent to item $toItem slot $toSlot not found for player $player" }
        return@on
    }
    player.inventory.swap(fromSlot, toSlot)
}

on<InterfaceOption>({ id == "inventory" && component == "container" }) { player: Player ->
    val itemDef = item.def
    val equipOption = when (optionIndex) {
        6 -> itemDef.options.getOrNull(3)
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionIndex)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $item $optionIndex" }
        return@on
    }
    player.events.emit(
        ContainerOption(
            id,
            item,
            itemIndex,
            equipOption
        )
    )
}