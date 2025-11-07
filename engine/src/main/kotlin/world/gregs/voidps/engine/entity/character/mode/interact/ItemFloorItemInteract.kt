package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class ItemFloorItemInteract(
    override val target: FloorItem,
    val item: Item,
    val slot: Int,
    val id: String,
    val player: Player,
    val approachRange: Int?
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.itemOnFloorItemBlocks.containsKey("${item.id}:*") || Operation.itemOnFloorItemBlocks.containsKey("${item.id}:${target.id}") || Operation.itemOnFloorItemBlocks.containsKey("*:${target.id}")

    override fun hasApproach() = Approachable.itemOnFloorItemBlocks.containsKey("${item.id}:*") || Approachable.itemOnFloorItemBlocks.containsKey("${item.id}:${target.id}") || Approachable.itemOnFloorItemBlocks.containsKey("*:${target.id}")

    override fun operate() {
        invoke(Operation.noDelays, Operation.itemOnFloorItemBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.itemOnFloorItemBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(ItemFloorItemInteract) -> Unit>>) {
        Script.launch {
            if (!noDelays.contains("${item.id}:${target.id}")) {
                player.arriveDelay()
            }
            for (block in map["${item.id}:${target.id}"] ?: map["${item.id}:*"]  ?: map["*:${target.id}"] ?: return@launch) {
                block(player, this@ItemFloorItemInteract)
            }
        }
    }
}