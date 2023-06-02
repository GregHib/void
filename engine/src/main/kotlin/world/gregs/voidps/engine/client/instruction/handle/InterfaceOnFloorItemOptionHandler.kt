package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnFloorItem
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItemStorage
import world.gregs.voidps.network.instruct.InteractInterfaceFloorItem

class InterfaceOnFloorItemOptionHandler(
    private val items: FloorItemStorage,
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
        val (id, component, item, container) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return
        player.mode = Interact(player, floorItem, InterfaceOnFloorItem(player, floorItem, id, component, item, itemSlot, container), approachRange = -1)
    }
}