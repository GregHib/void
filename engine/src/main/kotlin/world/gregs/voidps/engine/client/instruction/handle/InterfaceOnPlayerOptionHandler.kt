package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.client.instruction.InteractInterfacePlayer

class InterfaceOnPlayerOptionHandler(
    private val handler: InterfaceHandler,
) : InstructionHandler<InteractInterfacePlayer>() {

    override fun validate(player: Player, instruction: InteractInterfacePlayer): Boolean {
        val (playerIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val target = Players.indexed(playerIndex) ?: return false

        if ((interfaceId == 192 || interfaceId == 193) && itemId == -1) {
            player.closeInterfaces()
            player["magic_spell"] = "$interfaceId:$componentId"
            player["spellbook"] = if (interfaceId == 193) 1 else 0
            player.mode = ItemOnPlayerInteract(target, "$interfaceId:$componentId", Item.EMPTY, -1, player)
            return true
        }

        val (id, component, item) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot)
            ?: return false
        player.closeInterfaces()
        player.mode = ItemOnPlayerInteract(target, "$id:$component", item, itemSlot, player)

        return true
    }
}
