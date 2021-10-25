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

        val fromId = definitions.getId(fromInterfaceId)
        val fromDefinition = interfaceExists(player, fromId, fromComponentId) ?: return
        val fromComponent = fromDefinition.getComponentId(fromComponentId)

        val toId = definitions.getId(toInterfaceId)
        val toDefinition = interfaceExists(player, toId, toComponentId) ?: return
        val toComponent = toDefinition.getComponentId(toComponentId)
        sync {
            player.events.emit(
                InterfaceSwitch(
                    id = fromId,
                    component = fromComponent,
                    fromItemId = fromType,
                    fromSlot = fromSlot,
                    toId = toId,
                    toComponent = toComponent,
                    toItemId = toType,
                    toSlot = toSlot
                )
            )
        }
    }

    private fun interfaceExists(player: Player, toId: String, toComponentId: Int): InterfaceDefinition? {
        if (!player.interfaces.contains(toId)) {
            logger.debug { "Interface $toId not found for player $player" }
            return null
        }

        val definition = definitions.get(toId)
        val component = definition.components?.get(toComponentId)
        if (component == null) {
            logger.debug { "Interface $toId component $toComponentId not found for player $player" }
            return null
        }

        return definition
    }

}