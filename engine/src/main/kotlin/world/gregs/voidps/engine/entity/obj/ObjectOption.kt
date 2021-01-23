package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class ObjectOption(override val player: Player, val obj: GameObject, val option: String?, val partial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<ObjectOption>
}