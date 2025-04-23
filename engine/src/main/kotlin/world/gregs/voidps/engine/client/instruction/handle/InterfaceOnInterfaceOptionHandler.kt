package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnItem
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem

class InterfaceOnInterfaceOptionHandler(
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfaceItem>() {

    override fun validate(player: Player, instruction: InteractInterfaceItem) {
        val (fromItemId, toItemId, fromSlot, toSlot, fromInterfaceId, fromComponentId, toInterfaceId, toComponentId) = instruction

        val (fromId, fromComponent, fromItem, fromInventory) = handler.getInterfaceItem(player, fromInterfaceId, fromComponentId, fromItemId, fromSlot) ?: return
        val (_, _, toItem, toInventory) = handler.getInterfaceItem(player, toInterfaceId, toComponentId, toItemId, toSlot) ?: return

        player.closeInterfaces()
        player.queue.clearWeak()
        player.suspension = null
        val event = if (fromItem.isEmpty()) {
            InterfaceOnItem(
                fromId,
                fromComponent,
                fromSlot,
                toItem,
                toSlot,
                toInventory
            )
        } else {
            ItemOnItem(
                fromItem,
                toItem,
                fromSlot,
                toSlot,
                fromInventory,
                toInventory
            )
        }
        player.emit(event)
    }

}