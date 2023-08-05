package world.gregs.voidps.bot

import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.network.instruct.InterfaceClosedInstruction

suspend fun Bot.closeInterface(id: Int, component: Int) {
    player.instructions.emit(InterfaceClosedInstruction)
    clickInterface(id, component, 0)
}

suspend fun Bot.clickInterface(id: Int, component: Int, option: Int = 0, itemId: Int = -1, itemSlot: Int = -1) {
    player.instructions.emit(InteractInterface(interfaceId = id, componentId = component, itemId = itemId, itemSlot = itemSlot, option = option))
}