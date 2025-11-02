package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

data class InterfaceFloorItemInteract(
    override val target: FloorItem,
    val id: String,
    val index: Int,
    val player: Player,
    val approachRange: Int?
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.onObjectBlocks.containsKey("$id:*") || Operation.onObjectBlocks.containsKey("$id:${target.id}") || Operation.onObjectBlocks.containsKey("*:${target.id}")

    override fun hasApproach() = Approachable.onObjectBlocks.containsKey("$id:*") || Approachable.onObjectBlocks.containsKey("$id:${target.id}") || Approachable.onObjectBlocks.containsKey("*:${target.id}")

    override fun operate() {
        invoke(Operation.onFloorItemBlocks)
    }

    override fun approach() {
        invoke(Approachable.onFloorItemBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(InterfaceFloorItemInteract) -> Unit>>) {
        Events.events.launch {
            for (block in map["$id:${target.id}"] ?: map["$id:*"] ?: map["*:${target.id}"] ?: return@launch) {
                block(player, this@InterfaceFloorItemInteract)
            }
        }
    }
}