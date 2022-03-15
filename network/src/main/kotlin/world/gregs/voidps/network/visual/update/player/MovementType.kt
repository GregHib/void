package world.gregs.voidps.network.visual.update.player

import world.gregs.voidps.network.Visual

data class MovementType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(): Boolean {
        return type != MoveType.None
    }

    override fun reset() {
        type = MoveType.None
    }
}