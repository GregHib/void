package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface

suspend fun Bot.closeInterface(id: Int, component: Int) {
    player.instructions.emit(CloseInterface)
    clickInterface(id, component, 0)
}

suspend fun Bot.clickInterface(id: Int, component: Int, option: Int, itemId: Int = -1, itemSlot: Int = -1) {
    player.instructions.emit(InteractInterface(interfaceId = id, componentId = component, itemId = -1, itemSlot = itemSlot, option = option))
}