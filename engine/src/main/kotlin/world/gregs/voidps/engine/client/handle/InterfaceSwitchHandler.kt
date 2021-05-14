package world.gregs.voidps.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentName
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.MoveContainerItem
import world.gregs.voidps.utility.inject

class InterfaceSwitchHandler : Handler<MoveContainerItem>() {

    private val definitions: InterfaceDefinitions by inject()
    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: MoveContainerItem) {
        val (fromId, fromComponentId, fromType, fromSlot, toId, toComponentId, toType, toSlot) = instruction

        val from = interfaceExists(player, fromId, fromComponentId) ?: return
        val fromName = definitions.getName(fromId)
        val fromComponentName = from.getComponentName(fromComponentId)

        val to = interfaceExists(player, toId, toComponentId) ?: return
        val toName = definitions.getName(toId)
        val toComponentName = to.getComponentName(toComponentId)

        player.events.emit(
            InterfaceSwitch(
                fromId,
                fromName,
                fromComponentId,
                fromComponentName,
                fromType,
                fromSlot,
                toId,
                toName,
                toComponentId,
                toComponentName,
                toType,
                toSlot
            )
        )
    }

    private fun interfaceExists(player: Player, toId: Int, toComponentId: Int): InterfaceDefinition? {
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