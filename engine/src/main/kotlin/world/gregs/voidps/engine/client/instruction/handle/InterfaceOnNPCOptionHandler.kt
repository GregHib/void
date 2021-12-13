package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler : InstructionHandler<InteractInterfaceNPC>() {

    private val npcs: NPCs by inject()
    private val decoder: InterfaceDecoder by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = npcs.getAtIndex(npcIndex) ?: return

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
            val click = InterfaceOnNpcClick(
                npc,
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
            player.watch(npc)
            player.walkTo(npc) { path ->
                player.watch(null)
                if (path.steps.size == 0) {
                    player.face(npc)
                }
                if (path.result is PathResult.Failure) {
                    player.message("You can't reach that.")
                    return@walkTo
                }
                player.events.emit(
                    InterfaceOnNPC(
                        npc,
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