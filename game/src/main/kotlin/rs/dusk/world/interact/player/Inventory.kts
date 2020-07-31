package rs.dusk.world.interact.player

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.entity.character.contain.Containers
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.InterfaceItemsMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage
import rs.dusk.world.interact.player.display.InterfaceSwitch

InterfaceOpened where { name == "inventory" } then {
    player.send(InterfaceSettingsMessage(id, 0, 0, 27, 4554126))// Item slots
    player.send(InterfaceSettingsMessage(id, 0, 28, 55, 2097152))// Draggable slots
    player.send(InterfaceItemsMessage(Containers.Inventory.id, player.inventory.items, player.inventory.amounts))
}

private val logger = InlineLogger()

InterfaceSwitch where { name == "inventory" && toName == "inventory" } then {
    val container = player.inventory
    var fromItemId = fromItemId

    if(fromItemId == -1) {
        if (!container.inBounds(fromSlot)) {
            logger.warn { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@then
        }

        fromItemId = container.items[fromSlot]
    }

    if(!container.isValidId(fromSlot, fromItemId)) {
        logger.warn { "Interface $id component $componentId from item $fromItemId slot $fromSlot not found for player $player" }
        return@then
    }

    val toSlot = toSlot - 28
    if(!container.isValidId(toSlot, toItemId)) {
        logger.warn { "Interface $toId component $toComponentId to item $toItemId slot $toSlot not found for player $player" }
        return@then
    }
    player.inventory.switch(fromSlot, toSlot)
}