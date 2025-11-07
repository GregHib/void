package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

data class InterfaceObjectInteract(
    override val target: GameObject,
    val id: String,
    val index: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("$id:*") || Operation.onObjectBlocks.containsKey("$id:${target.def(player).stringId}") || Operation.onObjectBlocks.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("$id:*") || Approachable.onObjectBlocks.containsKey("$id:${target.def(player).stringId}") || Approachable.onObjectBlocks.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.onObjectBlocks)
    }

    override fun approach() {
        invoke(Approachable.onObjectBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(InterfaceObjectInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$id:${target.def(player).stringId}"] ?: map["$id:*"] ?: map["*:${target.def(player).stringId}"] ?: return@launch) {
                block(player, this@InterfaceObjectInteract)
            }
        }
    }
}