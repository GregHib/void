package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

data class ItemNPCInteract(
    override val target: NPC,
    val id: String,
    val item: Item,
    val slot: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onNpcBlocks.containsKey(id) || Operation.onNpcBlocks.containsKey(item.id)

    override fun hasApproach() = Approachable.onNpcBlocks.containsKey(id) || Approachable.onNpcBlocks.containsKey(item.id)

    override fun operate() {
        invoke(Operation.onNpcBlocks)
    }

    override fun approach() {
        invoke(Approachable.onNpcBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemNPCInteract) -> Unit>>) {
        Events.events.launch {
            for (block in map[id] ?: map[item.id] ?: return@launch) {
                block(player, this@ItemNPCInteract)
            }
        }
    }
}