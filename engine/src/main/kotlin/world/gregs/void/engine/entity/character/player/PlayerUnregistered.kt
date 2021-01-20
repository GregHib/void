package world.gregs.void.engine.entity.character.player

import world.gregs.void.engine.event.EventCompanion

data class PlayerUnregistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerUnregistered>
}