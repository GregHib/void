package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.character.mode.interact.clearInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.InteractInterfaceItem

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 20, 2021
 */
class InterfaceOnInterfaceOptionHandler(
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfaceItem>() {

    override fun validate(player: Player, instruction: InteractInterfaceItem) {
        val (fromItemId, toItemId, fromSlot, toSlot, fromInterfaceId, fromComponentId, toInterfaceId, toComponentId) = instruction

        val (fromId, fromComponent, fromItem, fromContainer) = handler.getInterfaceItem(player, fromInterfaceId, fromComponentId, fromItemId, fromSlot) ?: return
        val (toId, toComponent, toItem, toContainer) = handler.getInterfaceItem(player, toInterfaceId, toComponentId, toItemId, toSlot) ?: return

        player.clearInteract()
        player.events.emit(
            InterfaceOnInterface(
                fromItem,
                toItem,
                fromSlot,
                toSlot,
                fromId,
                fromComponent,
                toId,
                toComponent,
                fromContainer,
                toContainer
            )
        )
    }

}