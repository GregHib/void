package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

data class ItemPlayerInteract(
    override val target: Player,
    val id: String,
    val item: Item,
    val slot: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onPlayerBlocks.containsKey(id) || Operation.onPlayerBlocks.containsKey(item.id)

    override fun hasApproach() = Approachable.onPlayerBlocks.containsKey(id) || Approachable.onPlayerBlocks.containsKey(item.id)

    override fun operate() {
        invoke(Operation.onPlayerBlocks)
    }

    override fun approach() {
        invoke(Approachable.onPlayerBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemPlayerInteract) -> Unit>>) {
        Events.events.launch {
            for (block in map[id] ?: map[item.id] ?: return@launch) {
                block(player, this@ItemPlayerInteract)
            }
        }
    }
}