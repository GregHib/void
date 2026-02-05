package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler(private val handler: InterfaceHandler) : InstructionHandler<InteractInterfaceNPC>() {

    override fun validate(player: Player, instruction: InteractInterfaceNPC): Boolean {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = NPCs.indexed(npcIndex) ?: return false

        val (id, component, item) = handler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return false

        player.closeInterfaces()
        player.talkWith(npc)
        if (item.isEmpty()) {
            player.interactOn(npc, id, component, itemSlot)
        } else {
            player.interactItemOn(npc, id, component, item, itemSlot)
        }
        return true
    }
}

fun Player.interactItemOn(target: NPC, id: String, component: String, item: Item = Item.EMPTY, itemSlot: Int = -1) {
    mode = ItemOnNPCInteract(target, item, itemSlot, "$id:$component", this)
}

fun Player.interactOn(target: NPC, id: String, component: String, index: Int = -1) {
    mode = InterfaceOnNPCInteract(target, "$id:$component", index, this)
}