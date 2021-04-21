package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.InteractInterfaceNPC
import world.gregs.voidps.utility.inject

class InterfaceOnNPCOptionHandler : Handler<InteractInterfaceNPC>() {

    private val npcs: NPCs by inject()
    private val decoder: InterfaceDecoder by inject()
    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDetails: InterfaceDetails by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, id, componentId, itemId, itemSlot) = instruction
        val npc = npcs.getAtIndex(npcIndex) ?: return

        // Has interface open
        if (!player.interfaces.contains(id)) {
            logger.info { "Interface $id not found for player $player" }
            return
        }
        // Interface has that component
        val definition = decoder.get(id)
        val componentDef = definition.components?.get(componentId)
        if (componentDef == null) {
            logger.info { "Interface $id component $componentId not found for player $player" }
            return
        }

        // Get the string ids of the interface and component
        val inter = interfaceDetails.get(id)
        val componentName = inter.getComponentName(componentId)
        val component = inter.getComponent(componentName)
        val name = inter.name

        // If an item is provided
        var item = ""
        var containerName = ""
        if (itemId != -1 && itemSlot != -1) {
            // Check the component name is valid
            if (component == null) {
                logger.info { "Interface $name component $componentId not found for player $player" }
                return
            }
            // Check the component container exists
            containerName = component.container
            if (!player.hasContainer(containerName)) {
                logger.info { "Interface $name container $containerName not found for player $player" }
                return
            }

            // Check the item exists in the container
            val def = containerDefinitions.get(containerName)
            if (itemSlot > def.length) {
                logger.info { "Invalid interface $name container $containerName ${def.length} slot $itemSlot not found for player $player" }
                return
            }

            var found = false
            val primary = player.container(def, secondary = false)
            if (primary.isValidId(itemSlot, item)) {
                found = true
                item = itemDefinitions.getName(itemId)
            } else {
                val secondary = player.container(def, secondary = true)
                if (secondary.isValidId(itemSlot, item)) {
                    found = true
                    item = itemDefinitions.getName(itemId)
                }
            }
            if (!found) {
                logger.info { "Interface $name container item $item $itemSlot not found for player $player" }
                return
            }
        }


        player.walkTo(npc) { result ->
            player.watch(null)
            player.face(npc)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            player.events.emit(
                InterfaceOnNPC(
                    npc,
                    id,
                    name,
                    componentId,
                    componentName,
                    item,
                    itemId,
                    itemSlot,
                    containerName
                )
            )
        }
    }
}