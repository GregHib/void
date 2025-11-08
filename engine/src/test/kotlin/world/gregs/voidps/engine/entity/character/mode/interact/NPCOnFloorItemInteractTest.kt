package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.type.Tile

class NPCOnFloorItemInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("option"),
    )

    override val failedChecks = listOf(
        listOf("*"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcOperateFloorItem(args[0]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcApproachFloorItem(args[0]) {
            caller.call()
        }
    }

    override fun interact() = NPCOnFloorItemInteract(FloorItem(Tile.EMPTY, "floor_item"), "option", NPC("npc"), null)

}