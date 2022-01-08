package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfaceNPC

class InterfaceOnNPCOptionHandler : InstructionHandler<InteractInterfaceNPC>() {

    private val npcs: NPCs by inject()

    override fun validate(player: Player, instruction: InteractInterfaceNPC) {
        val (npcIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val npc = npcs.getAtIndex(npcIndex) ?: return

        val (id, component, item, container) = InterfaceHandler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        val click = InterfaceOnNpcClick(
            npc,
            id,
            component,
            item,
            itemSlot,
            container
        )
        player.events.emit(click)
        if (click.cancel) {
            return
        }
        player.watch(npc)
        player.walkTo(npc) { path ->
            player.watch(null)
            if (path.steps.size == 0) {
                player.face(npc)
            }
            player.interact(
                InterfaceOnNPC(
                    npc,
                    id,
                    component,
                    item,
                    itemSlot,
                    container
                )
            )
        }
    }
}