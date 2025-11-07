package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class ItemNPCInteract(
    override val target: NPC,
    val item: Item,
    val slot: Int,
    val id: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.itemOnNpcBlocks.containsKey("${item.id}:*") || Operation.itemOnNpcBlocks.containsKey("${item.id}:${target.def(player).stringId}") || Operation.itemOnNpcBlocks.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.itemOnNpcBlocks.containsKey("${item.id}:*") || Approachable.itemOnNpcBlocks.containsKey("${item.id}:${target.def(player).stringId}") || Approachable.itemOnNpcBlocks.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.itemOnNpcBlocks)
    }

    override fun approach() {
        invoke(Approachable.itemOnNpcBlocks)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map["${item.id}:${target.def(player).stringId}"] ?: map["*:${target.def(player).stringId}"] ?: map["${item.id}:*"] ?: return@launch) { // Hack for spells
                block(player, this@ItemNPCInteract)
            }
        }
    }
}