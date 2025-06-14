package world.gregs.voidps.network.login.protocol.visual.update.player

import world.gregs.voidps.network.login.protocol.Visual

data class TemporaryMoveType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(): Boolean = type != MoveType.None

    override fun reset() {
        type = MoveType.None
    }
}
