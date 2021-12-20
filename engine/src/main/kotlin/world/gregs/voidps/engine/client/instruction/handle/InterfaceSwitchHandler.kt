package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.instruct.MoveContainerItem

class InterfaceSwitchHandler : InstructionHandler<MoveContainerItem>() {

    override fun validate(player: Player, instruction: MoveContainerItem) = sync {
        var (fromInterfaceId, fromComponentId, fromItemId, fromSlot, toInterfaceId, toComponentId, toItemId, toSlot) = instruction
        if (toInterfaceId == 149) {
            toSlot -= 28
        }
        val (fromId, fromComponent, fromItem, fromContainer) = InterfaceHandler.getInterfaceItem(player, fromInterfaceId, fromComponentId, fromItemId, fromSlot) ?: return@sync
        val (toId, toComponent, toItem, toContainer) = InterfaceHandler.getInterfaceItem(player, toInterfaceId, toComponentId, toItemId, toSlot) ?: return@sync
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