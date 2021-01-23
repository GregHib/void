package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class ContainerAction(
    override val player: Player,
    val container: String,
    val item: String,
    val slot: Int,
    val option: String
) : PlayerEvent() {
        companion object : EventCompanion<ContainerAction>
}