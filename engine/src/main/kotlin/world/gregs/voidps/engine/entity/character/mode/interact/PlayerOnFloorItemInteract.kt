package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class PlayerOnFloorItemInteract(
    override val target: FloorItem,
    val option: String,
    val player: Player,
    val shape: Int?
) : Interact(player, target, shape = shape) {
    override fun hasOperate() = Operation.playerFloorItem.containsKey(option)

    override fun hasApproach() = Approachable.playerFloorItem.containsKey(option)

    override fun operate() {
        invoke(Operation.playerFloorItem)
    }

    override fun approach() {
        invoke(Approachable.playerFloorItem)
    }

    private fun invoke(map: Map<String, List<suspend Player.(PlayerOnFloorItemInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(player, this@PlayerOnFloorItemInteract)
            }
        }
    }

    override fun toString(): String {
        return "${player.name} ${player.tile} - $option target=$target, shape=$shape"
    }

}