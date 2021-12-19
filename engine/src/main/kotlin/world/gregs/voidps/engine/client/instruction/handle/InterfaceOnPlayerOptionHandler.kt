package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayerClick
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.sync
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfacePlayer

class InterfaceOnPlayerOptionHandler : InstructionHandler<InteractInterfacePlayer>() {

    private val players: Players by inject()

    override fun validate(player: Player, instruction: InteractInterfacePlayer) = sync {
        val (playerIndex, interfaceId, componentId, itemId, itemSlot) = instruction
        val target = players.getAtIndex(playerIndex) ?: return@sync

        val (id, component, item, container) = InterfaceHandler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return@sync

        val click = InterfaceOnPlayerClick(
            target,
            id,
            component,
            item,
            itemSlot,
            container
        )
        player.events.emit(click)
        if (click.cancel) {
            return@sync
        }
        player.face(target)
        player.walkTo(target) { path ->
//          player.face(null)
            if (path.steps.size == 0) {
                player.face(target)
            }
            if (path.result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            player.events.emit(
                InterfaceOnPlayer(
                    target,
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