package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.type.Area

data class AreaExited(
    override val player: Player,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, PlayerContext {
    override var onCancel: (() -> Unit)? = null
}