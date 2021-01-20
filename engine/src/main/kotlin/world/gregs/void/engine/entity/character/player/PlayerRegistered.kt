package world.gregs.void.engine.entity.character.player

import world.gregs.void.engine.event.EventCompanion

data class PlayerRegistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerRegistered>
}