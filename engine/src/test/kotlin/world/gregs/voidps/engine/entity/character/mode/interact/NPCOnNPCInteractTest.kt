package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC

class NPCOnNPCInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("option"),
    )
    
    override val failedChecks = listOf(
        listOf("*"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcOperateNPC(args[0]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcApproachNPC(args[0]) {
            caller.call()
        }
    }

    override fun interact() = NPCOnNPCInteract(NPC("npc_1"), "option", NPC("npc_2"))

}