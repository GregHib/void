package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.network.instruct.CloseInterface
import world.gregs.voidps.network.instruct.InteractInterface

suspend fun Bot.closeInterface(id: Int, component: Int) {
    player.instructions.emit(CloseInterface)
    player.instructions.emit(InteractInterface(interfaceId = id, componentId = component, itemId = -1, itemSlot = -1, option = 0))
}