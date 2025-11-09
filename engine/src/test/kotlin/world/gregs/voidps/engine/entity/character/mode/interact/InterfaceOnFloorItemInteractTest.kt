package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.type.Tile

class InterfaceOnFloorItemInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("id", "floor_item"),
        listOf("*", "floor_item"),
        listOf("id", "*"),
    )

    override val operate: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onFloorItemOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onFloorItemApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = InterfaceOnFloorItemInteract(FloorItem(Tile.EMPTY, "floor_item"), "id", 0, Player(), null)

}