package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.SuspendableEvent

abstract class Interaction : SuspendableEvent(), PlayerContext {
    abstract override val player: Player
}