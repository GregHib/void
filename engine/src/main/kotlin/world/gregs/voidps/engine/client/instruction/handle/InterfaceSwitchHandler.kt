package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentId
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.MoveContainerItem

class InterfaceSwitchHandler : InstructionHandler<MoveContainerItem>() {

    private val definitions: InterfaceDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: MoveContainerItem) {
        val (fromInterfaceId, fromComponentId, fromType, fromSlot, toInterfaceId, toComponentId, toType, toSlot) = instruction

        val fromDefinition = definitions.get(fromInterfaceId)
        if (!interfaceExists(player, fromDefinition, fromComponentId)) {
            return
        }
        val fromComponent = fromDefinition.getComponentId(fromComponentId)

        val toDefinition = definitions.get(toInterfaceId)
        if (!interfaceExists(player, toDefinition, toComponentId)) {
            return
        }
        val toComponent = toDefinition.getComponentId(toComponentId)
        sync {
            player.events.emit(
                InterfaceSwitch(
                    id = fromDefinition.stringId,
                    component = fromComponent,
                    fromItemId = fromType,
                    fromSlot = fromSlot,
                    toId = toDefinition.stringId,
                    toComponent = toComponent,
                    toItemId = toType,
                    toSlot = toSlot
                )
            )
        }
    }

    private fun interfaceExists(player: Player, definition: InterfaceDefinition, componentId: Int): Boolean {
        if (!player.interfaces.contains(definition.stringId)) {
            logger.debug { "Interface ${definition.stringId} not found for player $player" }
            return false
        }

        val component = definition.components?.get(componentId)
        if (component == null) {
            logger.debug { "Interface ${definition.stringId} component $componentId not found for player $player" }
            return false
        }

        return true
    }

}