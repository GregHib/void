package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.Operation
import world.gregs.voidps.engine.entity.character.player.Player

data class PlayerOnPlayerInteract(
    override val target: Player,
    val option: String,
    val player: Player,
) : Interact(player, target) {
    override fun hasOperate() = Operation.playerPlayer.containsKey(option)

    override fun hasApproach() = Approachable.playerPlayer.containsKey(option)

    override fun operate() {
        invoke(Operation.playerPlayer)
    }

    override fun approach() {
        invoke(Approachable.playerPlayer)
    }

    private fun invoke(map: Map<String, List<suspend Player.(PlayerOnPlayerInteract) -> Unit>>) {
        Script.launch {
            for (block in map[option] ?: return@launch) {
                block(player, this@PlayerOnPlayerInteract)
            }
        }
    }
}