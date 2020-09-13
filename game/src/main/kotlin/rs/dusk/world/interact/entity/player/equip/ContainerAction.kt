package rs.dusk.world.interact.entity.player.equip

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class ContainerAction(
    override val player: Player,
    val container: String,
    val item: String,
    val slot: Int,
    val option: String
) : PlayerEvent() {
        companion object : EventCompanion<ContainerAction>
}