package world.gregs.voidps.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject

val logger = InlineLogger()
val decoder: ItemDefinitions by inject()

on<InterfaceOpened>({ name == "inventory" }) { player: Player ->
    player.interfaceOptions.unlockAll(name, "container", 0 until 28)
    player.interfaceOptions.unlock(name, "container", 28 until 56, "Drag")
    player.sendContainer(name)
}

on<InterfaceSwitch>({ name == "inventory" && toName == "inventory" }) { player: Player ->
    val container = player.inventory
    var fromItemId = fromItemId

    if (fromItemId == -1) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@on
        }

        fromItemId = container.getItem(fromSlot)
    }

    if (!container.isValidId(fromSlot, fromItemId)) {
        logger.debug { "Interface $id component $componentId from item $fromItemId slot $fromSlot not found for player $player" }
        return@on
    }

    val toSlot = toSlot - 28
    if (!container.isValidId(toSlot, toItemId)) {
        logger.debug { "Interface $toId component $toComponentId to item $toItemId slot $toSlot not found for player $player" }
        return@on
    }
    player.inventory.swap(fromSlot, toSlot)
}

on<InterfaceOption>({ name == "inventory" && component == "container" }) { player: Player ->
    val itemDef = decoder.get(itemId)
    val equipOption = when (optionId) {
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionId)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $itemId $optionId" }
        return@on
    }
    player.events.emit(
        ContainerAction(
            name,
            item,
            itemIndex,
            equipOption
        )
    )
}