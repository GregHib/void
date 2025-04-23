package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
import world.gregs.voidps.engine.client.ui.interact.ItemOnPlayer
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.client.instruction.InteractInterfacePlayer

class InterfaceOnPlayerOptionHandler(
    private val players: Players,
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfacePlayer>() {

    override fun validate(player: Player, instruction: InteractInterfacePlayer) {
        val (playerIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val target = players.indexed(playerIndex) ?: return

        val (id, component, item, inventory) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        val interaction = if (item.isEmpty()) {
            InterfaceOnPlayer(
                player,
                target,
                id,
                component,
                itemSlot,
            )
        } else {
            ItemOnPlayer(
                player,
                target,
                item,
                itemSlot,
                inventory
            )
        }
        player.mode = Interact(player, target, interaction)
    }
}