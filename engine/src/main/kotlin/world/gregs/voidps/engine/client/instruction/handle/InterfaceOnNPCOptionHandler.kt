package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler(
    private val npcs: NPCs,
    private val handler: InterfaceHandler
) : InstructionHandler<InteractInterfaceNPC>() {

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = npcs.indexed(npcIndex) ?: return

        val (id, component, item, inventory) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        player.talkWith(npc)
        player.mode = Interact(player, npc, ItemOnNPC(
            player,
            npc,
            id,
            component,
            item,
            itemSlot,
            inventory
        ))
    }
}