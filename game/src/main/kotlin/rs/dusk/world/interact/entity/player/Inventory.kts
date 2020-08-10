package rs.dusk.world.interact.entity.player

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.contain.sendContainer
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

InterfaceOpened where { name == "inventory" } then {
    player.interfaces.sendSetting(name, "container", 0, 27, 4554126)// Item slots
    player.interfaces.sendSetting(name, "container", 28, 55, 2097152)// Draggable slots
    player.sendContainer(name)
}

private val logger = InlineLogger()

InterfaceSwitch where { name == "inventory" && toName == "inventory" } then {
    val container = player.inventory
    var fromItemId = fromItemId

    if(fromItemId == -1) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@then
        }

        fromItemId = container.items[fromSlot]
    }

    if(!container.isValidId(fromSlot, fromItemId)) {
        logger.debug { "Interface $id component $componentId from item $fromItemId slot $fromSlot not found for player $player" }
        return@then
    }

    val toSlot = toSlot - 28
    if(!container.isValidId(toSlot, toItemId)) {
        logger.debug { "Interface $toId component $toComponentId to item $toItemId slot $toSlot not found for player $player" }
        return@then
    }
    player.inventory.switch(fromSlot, toSlot)
}