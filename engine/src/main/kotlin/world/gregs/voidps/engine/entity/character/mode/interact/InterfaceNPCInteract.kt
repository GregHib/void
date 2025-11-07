package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

data class InterfaceNPCInteract(
    override val target: NPC,
    val id: String,
    val index: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onNpcBlocks.containsKey("$id:*") || Operation.onNpcBlocks.containsKey("$id:${target.def(player).stringId}") || Operation.onNpcBlocks.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.onNpcBlocks.containsKey("$id:*") || Approachable.onNpcBlocks.containsKey("$id:${target.def(player).stringId}") || Approachable.onNpcBlocks.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.onNpcBlocks)
    }

    override fun approach() {
        invoke(Approachable.onNpcBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(InterfaceNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$id:${target.def(player).stringId}"] ?: map["$id:*"] ?: map["*:${target.def(player).stringId}"] ?: return@launch) {
                block(player, this@InterfaceNPCInteract)
            }
        }
    }
}