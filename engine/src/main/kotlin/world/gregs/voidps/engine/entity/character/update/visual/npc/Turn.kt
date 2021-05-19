package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.map.Delta
import kotlin.math.atan2

data class Turn(
    var x: Int = 0,
    var y: Int = 0,
    var directionX: Int = 0,
    var directionY: Int = 0,
    var direction: Int = 0
) : Visual

const val TURN_MASK = 0x4

fun NPC.getTurn() = visuals.getOrPut(TURN_MASK) { Turn() }

fun NPC.flagTurn() = visuals.flag(TURN_MASK)

fun NPC.turn(entity: Entity, update: Boolean = true) {
    val delta = entity.tile.delta(tile)
    if (delta != Delta.EMPTY) {
        turn(delta.x, delta.y, update)
    }
}

fun NPC.turn(direction: Direction, update: Boolean = true) = turn(direction.delta.x, direction.delta.y, update)

fun NPC.turn(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val turn = getTurn()
    turn.x = tile.x
    turn.y = tile.y
    turn.directionX = deltaX
    turn.directionY = deltaY
    turn.direction = getFaceDirection(deltaX, deltaY)
    if (update) {
        flagTurn()
    }
}

fun getFaceDirection(xOffset: Int, yOffset: Int): Int {
    return (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
}
