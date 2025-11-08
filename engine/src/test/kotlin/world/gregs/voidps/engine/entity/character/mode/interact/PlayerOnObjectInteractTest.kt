package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

class PlayerOnObjectInteractTest : OnInteractTest() {

    override val checks = listOf(
        listOf("option", "obj"),
        listOf("option", "*"),
    )

    override val failedChecks = listOf(
        listOf("*", "obj"),
        listOf("*", "*"),
    )

    override val operate: Script.(List<String>, Caller) -> Unit = { args, caller ->
        objectOperate(args[0], args[1]) {
            caller.call()
        }
    }

    override val approach: Script.(List<String>, Caller) -> Unit = { args, caller ->
        objectApproach(args[0], args[1]) {
            caller.call()
        }
    }

    override fun interact() = PlayerOnObjectInteract(GameObject(0),"option", Player())

}