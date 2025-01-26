package content.bot

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.network.client.instruction.InterfaceClosedInstruction

suspend fun Bot.closeInterface(id: Int, component: Int) {
    player.instructions.send(InterfaceClosedInstruction)
    clickInterface(id, component, 0)
}

suspend fun Bot.clickInterface(id: Int, component: Int, option: Int = 0, itemId: Int = -1, itemSlot: Int = -1) {
    player.instructions.send(InteractInterface(interfaceId = id, componentId = component, itemId = itemId, itemSlot = itemSlot, option = option))
}

suspend fun Bot.itemOnObject(item: Item, obj: GameObject, interfaceId: Int = 149, component: Int = 0) {
    player.instructions.send(InteractInterfaceObject(obj.def.id, obj.tile.x, obj.tile.y, interfaceId, component, item.def.id, player.inventory.indexOf(item.id)))
}