package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class ItemOnFloorItemInteract(
    override val target: FloorItem,
    val item: Item,
    val slot: Int,
    val id: String,
    val player: Player,
    val approachRange: Int?
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.itemOnFloorItem.containsKey("${item.id}:*") || Operation.itemOnFloorItem.containsKey("${item.id}:${target.id}") || Operation.itemOnFloorItem.containsKey("*:${target.id}")

    override fun hasApproach() = Approachable.itemOnFloorItem.containsKey("${item.id}:*") || Approachable.itemOnFloorItem.containsKey("${item.id}:${target.id}") || Approachable.itemOnFloorItem.containsKey("*:${target.id}")

    override fun operate() {
        invoke(Operation.itemOnFloorItem)
    }

    override fun approach() {
        invoke(Approachable.itemOnFloorItem)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemOnFloorItemInteract) -> Unit>>) {
        Script.launch {
            for (block in map["${item.id}:${target.id}"] ?: map["${item.id}:*"]  ?: map["*:${target.id}"] ?: return@launch) {
                block(player, this@ItemOnFloorItemInteract)
            }
        }
    }

    override fun toString(): String {
        return "${player.name} ${player.tile} - ${item.id}:${target.id} target=$target, item=$item, slot=$slot, interface='$id', approachRange=$approachRange"
    }

}