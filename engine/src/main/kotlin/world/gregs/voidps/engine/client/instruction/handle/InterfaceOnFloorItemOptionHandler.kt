package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnFloorItem
import world.gregs.voidps.engine.client.ui.interact.ItemOnFloorItem
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.network.client.instruction.InteractInterfaceFloorItem

class InterfaceOnFloorItemOptionHandler(
    private val items: FloorItems,
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfaceFloorItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractInterfaceFloorItem) {
        val (floorItemId, x, y, interfaceId, componentId, itemId, itemSlot) = instruction
        val tile = player.tile.copy(x, y)
        val floorItem = items[tile].firstOrNull { it.def.id == floorItemId }
        if (floorItem == null) {
            logger.warn { "Invalid floor item $itemId $tile" }
            return
        }
        val (id, component, item, inventory) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return
        val interaction = if (item.isEmpty()) {
            InterfaceOnFloorItem(player, floorItem, id, component, itemSlot)
        } else {
            ItemOnFloorItem(player, floorItem, item, itemSlot, inventory)
        }
        player.closeInterfaces()
        player.mode = Interact(player, floorItem, interaction, approachRange = -1)
    }
}