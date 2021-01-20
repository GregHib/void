package world.gregs.void.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import world.gregs.void.engine.client.ui.event.InterfaceOpened
import world.gregs.void.engine.entity.character.contain.inventory
import world.gregs.void.engine.entity.character.contain.sendContainer
import world.gregs.void.engine.entity.definition.ItemDefinitions
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.player.display.InterfaceOption
import world.gregs.void.world.interact.entity.player.display.InterfaceSwitch

val logger = InlineLogger()
val decoder: ItemDefinitions by inject()
val bus: EventBus by inject()

InterfaceOpened where { name == "inventory" } then {
    player.interfaceOptions.unlockAll(name, "container", 0 until 28)
    player.interfaceOptions.unlock(name, "container", 28 until 56, "Drag")
    player.sendContainer(name)
}

InterfaceSwitch where { name == "inventory" && toName == "inventory" } then {
    val container = player.inventory
    var fromItemId = fromItemId

    if (fromItemId == -1) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@then
        }

        fromItemId = container.getItem(fromSlot)
    }

    if (!container.isValidId(fromSlot, fromItemId)) {
        logger.debug { "Interface $id component $componentId from item $fromItemId slot $fromSlot not found for player $player" }
        return@then
    }

    val toSlot = toSlot - 28
    if (!container.isValidId(toSlot, toItemId)) {
        logger.debug { "Interface $toId component $toComponentId to item $toItemId slot $toSlot not found for player $player" }
        return@then
    }
    player.inventory.swap(fromSlot, toSlot)
}

InterfaceOption where { name == "inventory" && component == "container" } then {
    val itemDef = decoder.get(itemId)
    val equipOption = when (optionId) {
        7 -> itemDef.options.getOrNull(4)
        9 -> "Examine"
        else -> itemDef.options.getOrNull(optionId)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $itemId $optionId" }
        return@then
    }
    bus.emit(
        ContainerAction(
            player,
            name,
            item,
            itemIndex,
            equipOption
        )
    )
}