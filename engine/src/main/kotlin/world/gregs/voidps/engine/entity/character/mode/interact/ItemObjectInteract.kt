package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

data class ItemObjectInteract(
    override val target: GameObject,
    val item: Item,
    val slot: Int,
    val id: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.itemOnObjectBlocks.containsKey("${item.id}:*") || Operation.itemOnObjectBlocks.containsKey("${item.id}:${target.def(player).stringId}") || Operation.itemOnObjectBlocks.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.itemOnObjectBlocks.containsKey("${item.id}:*") || Approachable.itemOnObjectBlocks.containsKey("${item.id}:${target.def(player).stringId}") || Approachable.itemOnObjectBlocks.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.noDelays, Operation.itemOnObjectBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.itemOnObjectBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(ItemObjectInteract) -> Unit>>) {
        Script.launch {
            if (!noDelays.contains("${item.id}:${target.def(player).stringId}")) {
                player.arriveDelay()
            }
            for (block in map["${item.id}:${target.def(player).stringId}"] ?: map["${item.id}:*"]  ?: map["*:${target.def(player).stringId}"] ?: return@launch) {
                block(player, this@ItemObjectInteract)
            }
        }
    }
}