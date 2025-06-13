package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
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
        val obj = objects[tile, objectId]
        if (obj == null) {
            player.noInterest()
            return
        }

        val (id, component, item, inventory) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return
        val interaction = if (item.isEmpty()) {
            InterfaceOnObject(
                player,
                obj,
                id,
                component,
                itemSlot,
            )
        } else {
            ItemOnObject(
                player,
                obj,
                item,
                itemSlot,
                inventory,
            )
        }
        player.closeInterfaces()
        player.mode = Interact(player, obj, interaction)
    }
}
