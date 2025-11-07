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
    override fun hasOperate() = Operation.itemOnObject.containsKey("${item.id}:*") || Operation.itemOnObject.containsKey("${item.id}:${target.def(player).stringId}") || Operation.itemOnObject.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.itemOnObject.containsKey("${item.id}:*") || Approachable.itemOnObject.containsKey("${item.id}:${target.def(player).stringId}") || Approachable.itemOnObject.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.noDelays, Operation.itemOnObject)
    }

    override fun approach() {
        invoke(emptySet(), Approachable.itemOnObject)
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