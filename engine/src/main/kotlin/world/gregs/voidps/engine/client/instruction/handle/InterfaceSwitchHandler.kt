package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.MoveInventoryItem

class InterfaceSwitchHandler(
    private val handler: InterfaceHandler,
) : InstructionHandler<MoveInventoryItem>() {

    override fun validate(player: Player, instruction: MoveInventoryItem) {
        var (fromInterfaceId, fromComponentId, fromItemId, fromSlot, toInterfaceId, toComponentId, toItemId, toSlot) = instruction
        if (toInterfaceId == 149) {
            toSlot -= 28
            val temp = fromItemId
            fromItemId = toItemId
            toItemId = temp
        }
        val (fromId, fromComponent) = handler.getInterfaceItem(player, fromInterfaceId, fromComponentId, fromItemId, fromSlot) ?: return
        val (toId, toComponent) = handler.getInterfaceItem(player, toInterfaceId, toComponentId, toItemId, toSlot) ?: return
        InterfaceApi.swap(player, "$fromId:$fromComponent", "$toId:$toComponent", fromSlot, toSlot)
    }
}
