package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class InterfaceOnFloorItemInteract(
    override val target: FloorItem,
    val id: String,
    val index: Int,
    val player: Player,
    val approachRange: Int?
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.onFloorItem.containsKey("$id:*") || Operation.onFloorItem.containsKey("$id:${target.id}") || Operation.onFloorItem.containsKey("*:${target.id}")

    override fun hasApproach() = Approachable.onFloorItem.containsKey("$id:*") || Approachable.onFloorItem.containsKey("$id:${target.id}") || Approachable.onFloorItem.containsKey("*:${target.id}")

    override fun operate() {
        invoke(Operation.onFloorItem)
    }

    override fun approach() {
        invoke(Approachable.onFloorItem)
    }

    private fun invoke(map: Map<String, List<suspend Player.(InterfaceOnFloorItemInteract) -> Unit>>) {
        Script.launch {
            for (block in map["$id:${target.id}"] ?: map["$id:*"] ?: map["*:${target.id}"] ?: return@launch) {
                block(player, this@InterfaceOnFloorItemInteract)
            }
        }
    }

    override fun toString(): String {
        return "${player.name} ${player.tile} - $id:${target.id} target=$target, interface='$id', index=$index, approachRange=$approachRange"
    }

}