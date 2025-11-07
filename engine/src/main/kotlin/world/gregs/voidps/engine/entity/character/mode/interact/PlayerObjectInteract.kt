package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

data class PlayerObjectInteract(
    override val target: GameObject,
    val option: String,
    val player: Player,
    var approachRange: Int? = null,
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.playerObjectBlocks.containsKey("$option:${target.def(player).stringId}") || Operation.playerObjectBlocks.containsKey("$option:*")

    override fun hasApproach() = Approachable.playerObjectBlocks.containsKey("$option:${target.def(player).stringId}") || Approachable.playerObjectBlocks.containsKey("$option:*")

    override fun operate() {
        invoke(Operation.noDelays, Operation.playerObjectBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.playerObjectBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(PlayerObjectInteract) -> Unit>>) {
        Script.launch {
            val id = target.def(player).stringId
            if (!noDelays.contains("$option:$id") && (!noDelays.contains("$option:*") && !map.containsKey("$option:$id"))) {
                player.arriveDelay()
            }
            for (block in map["$option:$id"] ?: map["$option:*"] ?: return@launch) {
                block(player, this@PlayerObjectInteract)
            }
        }
    }
}