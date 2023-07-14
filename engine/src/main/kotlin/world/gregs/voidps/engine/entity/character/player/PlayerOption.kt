package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction

data class PlayerOption(
    override val player: Player,
    val target: Player,
    val option: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}