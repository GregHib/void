package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObjectClick
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfaceObject

class InterfaceOnObjectOptionHandler : InstructionHandler<InteractInterfaceObject>() {

    private val objects: Objects by inject()
    private val decoder: InterfaceDecoder by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceObject) {
        val (objectId, x, y, interfaceId, componentId, itemId, itemSlot) = instruction
        val tile = Tile(x, y, player.tile.plane)
        val obj = objects[tile, objectId] ?: return

        // Has interface open
        val id = interfaceDefinitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.info { "Interface $interfaceId not found for player $player" }
            return
        }
        // Interface has that component
        val definition = decoder.get(interfaceId)
        val componentDef = definition.components?.get(componentId)
        if (componentDef == null) {
            logger.info { "Interface $interfaceId component $componentId not found for player $player" }
            return
        }

        // Get the string ids of the interface and component
        val component = definition.getComponentId(componentId)
        val componentDefinition = definition.getComponentOrNull(component)

        // If an item is provided
        var item = Item.EMPTY
        var containerName = ""
        if (itemId != -1 && itemSlot != -1) {
            // Check the component name is valid
            if (componentDefinition == null) {
                logger.info { "Interface $id component $componentId not found for player $player" }
                return
            }
            // Check the component container exists
            containerName = componentDefinition["container", ""]
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $id container $containerName not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(containerName)
            if (itemSlot > def.length) {
                logger.info { "Invalid interface $id container $containerName ${def.length} slot $itemSlot not found for player $player" }
                return
            }

            var found = false
            val primary = player.container(def, secondary = false)
            if (primary.isValidId(itemSlot, itemDefinitions.get(itemId).stringId)) {
                found = true
                item = primary.getItem(itemSlot)
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(itemSlot, itemDefinitions.get(itemId).stringId)) {
                    found = true
                    item = secondary.getItem(itemSlot)
                }
            }
            if (!found) {
                logger.info { "Interface $id container item $item $itemSlot not found for player $player" }
                return
            }
        }

        sync {
            val click = InterfaceOnObjectClick(
                obj,
                id,
                component,
                item,
                itemSlot,
                containerName
            )
            player.events.emit(click)
            if (click.cancel) {
                return@sync
            }
            player.face(obj)
            player.walkTo(obj) { path ->
//                player.face(null)
                if (path.steps.size == 0) {
                    player.face(obj)
                }
                if (path.result is PathResult.Failure) {
                    player.message("You can't reach that.")
                    return@walkTo
                }
                player.events.emit(
                    InterfaceOnObject(
                        obj,
                        id,
                        component,
                        item,
                        itemSlot,
                        containerName
                    )
                )
            }
        }
    }
}