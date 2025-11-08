package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.type.Tile

class InterfaceOnNPCInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("id", "npc"),
        listOf("*", "npc"),
        listOf("id", "*"),
    )
    
    override val operate: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onNPCOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onNPCApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = InterfaceOnNPCInteract(NPC("npc"), "id", 0, Player())

}