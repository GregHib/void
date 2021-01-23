package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.event.EventCompanion

data class PlayerRegistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerRegistered>
}