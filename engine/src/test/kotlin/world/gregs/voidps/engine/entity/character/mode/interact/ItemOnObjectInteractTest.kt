package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

class ItemOnObjectInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("item", "obj"),
        listOf("item", "*"),
        listOf("*", "obj"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnObjectOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnObjectApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = ItemOnObjectInteract(GameObject(0), Item("item"), 0, "id", Player())

}