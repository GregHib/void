package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem

data class PlayerFloorItemInteract(
    override val target: FloorItem,
    val option: String,
    val player: Player,
    val shape: Int?
) : Interact(player, target, shape = shape) {
    override fun hasOperate() = Operation.playerFloorItem.containsKey(option)

    override fun hasApproach() = Approachable.playerFloorItem.containsKey(option)

    override fun operate() {
        invoke(Operation.noDelays, Operation.playerFloorItem)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.playerFloorItem)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(PlayerFloorItemInteract) -> Unit>>) {
        Script.launch {
            if (!noDelays.contains(option)) {
                player.arriveDelay()
            }
            for (block in map[option] ?: return@launch) {
                block(player, this@PlayerFloorItemInteract)
            }
        }
    }
}