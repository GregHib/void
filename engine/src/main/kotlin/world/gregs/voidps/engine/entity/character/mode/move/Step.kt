package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.type.Tile

typealias Step = Tile

private fun id(tile: Int, noCollision: Boolean = false, slowRun: Boolean = false) =
    tile + (noCollision.toInt() shl 30) + (slowRun.toInt() shl 31)

private fun noCollision(id: Int) = id shr 30 and 0x1 == 1
private fun slowRun(id: Int) = id shr 31 and 0x1 == 1

internal fun Tile.step(noCollision: Boolean, slowRun: Boolean): Step {
    if (!noCollision && !slowRun) {
        return this
    }
    return Tile(id(id, noCollision, slowRun))
}

val Step.noCollision: Boolean
    get() = noCollision(id)

val Step.slowRun: Boolean
    get() = slowRun(id)