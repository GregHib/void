package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Events

data class PlayerObjectInteract(
    override val target: GameObject,
    val option: String,
    val player: Player,
    var approachRange: Int? = null
) : Interact(player, target, approachRange = approachRange) {
    override fun hasOperate() = Operation.playerObjectBlocks.containsKey(option)

    override fun hasApproach() = Approachable.playerObjectBlocks.containsKey(option)

    override fun operate() {
        invoke(Operation.noDelays, Operation.playerObjectBlocks)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.playerObjectBlocks)
    }

    private fun invoke(noDelays: Set<String>, map: Map<String, List<suspend Player.(PlayerObjectInteract) -> Unit>>) {
        Events.events.launch {
            val id = target.def(player).stringId
            if (!noDelays.contains("$option:$id")) {
                player.arriveDelay()
            }
            for (block in map["$option:*"] ?: return@launch) {
                block(player, this@PlayerObjectInteract)
            }
        }
    }
}