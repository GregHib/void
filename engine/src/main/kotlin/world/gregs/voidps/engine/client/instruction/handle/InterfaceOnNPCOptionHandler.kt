package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.clearInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler(
    private val npcs: NPCs,
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfaceNPC>() {

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = npcs.indexed(npcIndex) ?: return

        val (id, component, item, container) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        val click = InterfaceOnNpcClick(
            npc,
            id,
            component,
            item,
            itemSlot,
            container
        )
        player.events.emit(click)
        if (click.cancelled) {
            return
        }
        player.clearInteract()
        player.mode = Interact(player, npc, InterfaceOnNPC(
            player,
            npc,
            id,
            component,
            item,
            itemSlot,
            container
        ))
    }
}