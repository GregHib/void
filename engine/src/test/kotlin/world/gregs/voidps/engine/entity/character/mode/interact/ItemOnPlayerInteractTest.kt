package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

class ItemOnPlayerInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("item"),
        listOf("*"),
        listOf("id"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnPlayerOperate(args[0]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnPlayerApproach(args[0]) {
            caller.call()
        }
    }

    override fun interact() = ItemOnPlayerInteract(Player(0), "id", Item("item"), 0, Player(1))

}