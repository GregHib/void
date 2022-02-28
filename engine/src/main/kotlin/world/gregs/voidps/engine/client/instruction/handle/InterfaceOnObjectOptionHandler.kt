package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObjectClick
import world.gregs.voidps.engine.entity.character.move.interact
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.InteractInterfaceObject

class InterfaceOnObjectOptionHandler : InstructionHandler<InteractInterfaceObject>() {

    private val objects: Objects by inject()

    override fun validate(player: Player, instruction: InteractInterfaceObject) {
        val (objectId, x, y, interfaceId, componentId, itemId, itemSlot) = instruction
        val tile = Tile(x, y, player.tile.plane)
        val obj = objects[tile, objectId]
        if (obj == null) {
            player.noInterest()
            return
        }

        val (id, component, item, container) = InterfaceHandler.getInterfaceItem(player, interfaceId, componentId, itemId, itemSlot) ?: return

        val click = InterfaceOnObjectClick(
            obj,
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
        player.face(obj)
        player.walkTo(obj, cancelAction = true) { path ->
//          player.face(null)
            if (path.steps.size == 0) {
                player.face(obj)
            }
            player.interact(
                InterfaceOnObject(
                    obj,
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