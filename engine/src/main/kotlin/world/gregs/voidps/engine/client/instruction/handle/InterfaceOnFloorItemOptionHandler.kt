package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.client.instruction.InteractInterfaceFloorItem

class InterfaceOnFloorItemOptionHandler(private val handler: InterfaceHandler) : InstructionHandler<InteractInterfaceFloorItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceFloorItem): Boolean {
        val (floorItemId, x, y, interfaceId, componentId, itemId, itemSlot) = instruction
        val tile = player.tile.copy(x, y)
        val floorItem = FloorItems.at(tile).firstOrNull { it.def.id == floorItemId }
        if (floorItem == null) {
            logger.warn { "Invalid floor item $itemId $tile" }
            return false
        }
        val (id, component, item) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return false
        player.closeInterfaces()
        if (item.isEmpty()) {
            player.interactOn(floorItem, id, component, itemSlot, approachRange = -1)
        } else {
            player.interactItemOn(floorItem, id, component, item, itemSlot, approachRange = -1)
        }
        return true
    }
}


fun Player.interactItemOn(target: FloorItem, id: String, component: String, item: Item = Item.EMPTY, itemSlot: Int = -1, approachRange: Int? = null) {
    mode = ItemOnFloorItemInteract(target, item, itemSlot, "$id:$component", this, approachRange)
}

fun Player.interactOn(target: FloorItem, id: String, component: String, index: Int = -1, approachRange: Int? = null) {
    mode = InterfaceOnFloorItemInteract(target, "$id:$component", index, this, approachRange)
}
