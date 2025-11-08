package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class ItemOnPlayerInteract(
    override val target: Player,
    val id: String,
    val item: Item,
    val slot: Int,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.onPlayer.containsKey(id) || Operation.onPlayer.containsKey(item.id)

    override fun hasApproach() = Approachable.onPlayer.containsKey(id) || Approachable.onPlayer.containsKey(item.id)

    override fun operate() {
        invoke(Operation.onPlayer)
    }

    override fun approach() {
        invoke(Approachable.onPlayer)
    }

    private fun invoke(map: Map<String, List<suspend Player.(ItemOnPlayerInteract) -> Unit>>) {
        Script.launch {
            for (block in map[id] ?: map[item.id] ?: return@launch) {
                block(player, this@ItemOnPlayerInteract)
            }
        }
    }
}