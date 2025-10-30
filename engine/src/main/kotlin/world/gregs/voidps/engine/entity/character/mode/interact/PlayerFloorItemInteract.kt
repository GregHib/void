package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Events

data class PlayerFloorItemInteract(
    override val target: FloorItem,
    val option: String,
    val player: Player,
    val shape: Int?
) : Interact(player, target, shape = shape) {
    override fun hasOperate() = Operation.playerFloorItemBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerFloorItemBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.noDelays, Operation.playerFloorItemBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.playerFloorItemBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(PlayerFloorItemInteract) -> Unit>>) {
        Events.events.launch {
            if (!noDelays.contains(option)) {
                player.arriveDelay()
            }
            for (block in map[option] ?: return@launch) {
                block(player, this@PlayerFloorItemInteract)
            }
        }
    }
}