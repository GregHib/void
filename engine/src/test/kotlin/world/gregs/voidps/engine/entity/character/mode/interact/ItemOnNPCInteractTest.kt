package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class ItemOnNPCInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("item", "npc"),
        listOf("item", "*"),
        listOf("*", "npc"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnNPCOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        itemOnNPCApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = ItemOnNPCInteract(NPC("npc", def = NPCDefinition(0, stringId = "npc")), Item("item"), 0, "id", Player())

}