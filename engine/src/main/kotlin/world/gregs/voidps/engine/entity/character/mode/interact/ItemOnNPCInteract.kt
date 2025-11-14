package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item

data class ItemOnNPCInteract(
    override val target: NPC,
    val item: Item,
    val slot: Int,
    val id: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.itemOnNpc.containsKey("${item.id}:*") || Operation.itemOnNpc.containsKey("${item.id}:${target.def(player).stringId}") || Operation.itemOnNpc.containsKey("*:${target.def(player).stringId}")

    override fun hasApproach() = Approachable.itemOnNpc.containsKey("${item.id}:*") || Approachable.itemOnNpc.containsKey("${item.id}:${target.def(player).stringId}") || Approachable.itemOnNpc.containsKey("*:${target.def(player).stringId}")

    override fun operate() {
        invoke(Operation.itemOnNpc)
    }

    override fun approach() {
        invoke(Approachable.itemOnNpc)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemOnNPCInteract) -> Unit>>) {
        Script.launch {
            for (block in map["${item.id}:${target.def(player).stringId}"] ?: map["*:${target.def(player).stringId}"] ?: map["${item.id}:*"] ?: return@launch) { // Hack for spells
                block(player, this@ItemOnNPCInteract)
            }
        }
    }

    override fun toString(): String {
        return "${player.name} ${player.tile} - ${item.id}:${target.def(player).stringId} target=$target, item=$item, slot=$slot, interface='$id'"
    }

}