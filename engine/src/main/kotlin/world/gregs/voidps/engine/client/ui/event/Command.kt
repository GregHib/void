package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerInteraction
import world.gregs.voidps.engine.entity.character.player.Player

data class Command(
    override val player: Player,
    val prefix: String,
    val content: String
) : PlayerInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}