package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.SuspendableEvent

abstract class Interaction : SuspendableEvent() {
    abstract val player: Player
}