package rs.dusk.engine.entity.character.player

import rs.dusk.engine.event.EventCompanion

data class PlayerUnregistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerUnregistered>
}