package world.gregs.void.engine.entity.obj

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class ObjectOption(override val player: Player, val obj: GameObject, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<ObjectOption>
}