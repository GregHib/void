package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.type.Tile

class ItemOnFloorItemInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("item", "floor_item"),
        listOf("item", "*"),
        listOf("*", "floor_item"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnFloorItemOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnFloorItemApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = ItemOnFloorItemInteract(FloorItem(Tile.EMPTY, "floor_item"), Item("item"), 0, "id", Player(), null)

}