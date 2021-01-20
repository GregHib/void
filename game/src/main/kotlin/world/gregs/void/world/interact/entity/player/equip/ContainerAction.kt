package world.gregs.void.world.interact.entity.player.equip

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class ContainerAction(
    override val player: Player,
    val container: String,
    val item: String,
    val slot: Int,
    val option: String
) : PlayerEvent() {
        companion object : EventCompanion<ContainerAction>
}