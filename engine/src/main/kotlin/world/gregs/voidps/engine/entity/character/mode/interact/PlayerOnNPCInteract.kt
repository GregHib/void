package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class PlayerOnNPCInteract(
    override val target: NPC,
    val option: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.playerNpc.containsKey("$option:${target.def(player).stringId}") || Operation.playerNpc.containsKey("$option:*")

    override fun hasApproach() = Approachable.playerNpc.containsKey("$option:${target.def(player).stringId}") || Approachable.playerNpc.containsKey("$option:*")

    override fun operate() {
        invoke(Operation.playerNpc)
    }

    override fun approach() {
        invoke(Approachable.playerNpc)
    }

    private fun invoke(map: Map<String, List<suspend Player.(PlayerOnNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$option:${target.def(player).stringId}"] ?: map["$option:*"] ?: return@launch) {
                block(player, this@PlayerOnNPCInteract)
            }
        }
    }
}