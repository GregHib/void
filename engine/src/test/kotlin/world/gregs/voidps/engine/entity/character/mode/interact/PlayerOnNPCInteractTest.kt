package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class PlayerOnNPCInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("option", "npc"),
        listOf("option", "*"),
    )

    override val failedChecks = listOf(
        listOf("*", "npc"),
        listOf("*", "*"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = PlayerOnNPCInteract(NPC("npc"),"option", Player())

}