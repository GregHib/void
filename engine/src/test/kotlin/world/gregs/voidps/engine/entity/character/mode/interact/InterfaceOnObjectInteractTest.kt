package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

class InterfaceOnObjectInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("id", "obj"),
        listOf("*", "obj"),
        listOf("id", "*"),
    )
    
    override val operate: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onObjectOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(args: List<String>, caller: Caller) -> Unit = { args, caller ->
        onObjectApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = InterfaceOnObjectInteract(GameObject(0), "id", 0, Player())

}