package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.type.Tile

class InterfaceOnObjectOptionHandler(
    private val objects: GameObjects,
    private val handler: InterfaceHandler,
) : InstructionHandler<InteractInterfaceObject>() {

    override fun validate(player: Player, instruction: InteractInterfaceObject) {
        val (objectId, x, y, interfaceId, componentId, itemId, itemSlot) = instruction
        val tile = Tile(x, y, player.tile.level)
        val obj = objects.findOrNull(tile, objectId)
        if (obj == null) {
            player.noInterest()
            return
        }

        val (id, component, item) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return
        player.closeInterfaces()
        if (item.isEmpty()) {
            player.interactOn(obj, id, component, itemSlot)
        } else {
            player.interactItemOn(obj, id, component, item, itemSlot)
        }
    }
}

fun Player.interactItemOn(target: GameObject, id: String, component: String, item: Item = Item.EMPTY, itemSlot: Int = -1) {
    mode = ItemOnObjectInteract(target, item, itemSlot, "$id:$component", this)
}

fun Player.interactOn(target: GameObject, id: String, component: String, index: Int = -1) {
    mode = InterfaceOnObjectInteract(target, "$id:$component", index, this)
}