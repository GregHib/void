package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObject

class NPCOnObjectInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("option", "obj"),
        listOf("option", "*"),
    )
    
    override val failedChecks = listOf(
        listOf("*", "obj"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcOperateObject(args[0]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        npcApproachObject(args[0]) {
            caller.call()
        }
    }

    override fun interact() = NPCOnObjectInteract(GameObject(0), "option", NPC("npc"))

}