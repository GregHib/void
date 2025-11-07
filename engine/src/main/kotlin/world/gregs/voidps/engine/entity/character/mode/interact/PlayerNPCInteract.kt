package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class PlayerNPCInteract(
    override val target: NPC,
    val option: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.playerNpcBlocks.containsKey("$option:${target.def(player).stringId}") || Operation.playerNpcBlocks.containsKey("$option:*")

    override fun hasApproach() = Approachable.playerNpcBlocks.containsKey("$option:${target.def(player).stringId}") || Approachable.playerNpcBlocks.containsKey("$option:*")

    override fun operate() {
        invoke(Operation.playerNpcBlocks)
    }

    override fun approach() {
        invoke(Approachable.playerNpcBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(PlayerNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$option:${target.def(player).stringId}"] ?: map["$option:*"] ?: return@launch) {
                block(player, this@PlayerNPCInteract)
            }
        }
    }
}