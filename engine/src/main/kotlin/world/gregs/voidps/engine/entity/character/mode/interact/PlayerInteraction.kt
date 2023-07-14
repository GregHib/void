package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext

abstract class PlayerInteraction : Interaction(), PlayerContext {
    abstract override val player: Player
    override var onCancel: (() -> Unit)? = { player.clearAnimation() }
}