package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.MoveContainerItem

class InterfaceSwitchHandler(
    private val handler: InterfaceHandler
) : InstructionHandler<MoveContainerItem>() {

    override fun validate(player: Player, instruction: MoveContainerItem) {
        var (fromInterfaceId, fromComponentId, fromItemId, fromSlot, toInterfaceId, toComponentId, toItemId, toSlot) = instruction
        if (toInterfaceId == 149) {
            toSlot -= 28
            val temp = fromItemId
            fromItemId = toItemId
            toItemId = temp
        }
        val (fromId, fromComponent, fromItem, fromContainer) = handler.getInterfaceItem(player, fromInterfaceId, fromComponentId, fromItemId, fromSlot) ?: return
        val (toId, toComponent, toItem, toContainer) = handler.getInterfaceItem(player, toInterfaceId, toComponentId, toItemId, toSlot) ?: return

        player.closeDialogue()
        player.queue.clearWeak()
        player.events.emit(
            InterfaceSwitch(
                id = fromId,
                component = fromComponent,
                fromItem = fromItem,
                fromSlot = fromSlot,
                fromContainer = fromContainer,
                toId = toId,
                toComponent = toComponent,
                toItem = toItem,
                toSlot = toSlot,
                toContainer = toContainer
            )
        )
    }
}