package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.interact.ItemNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler(
    private val npcs: NPCs,
    private val handler: InterfaceHandler,
) : InstructionHandler<InteractInterfaceNPC>() {

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = npcs.indexed(npcIndex) ?: return

        val (id, component, item) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        player.closeInterfaces()
        player.talkWith(npc)
        player.mode = ItemNPCInteract(npc, "$id:$component", item, itemSlot, player)
    }
}
