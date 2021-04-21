package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.map.Delta
import kotlin.math.atan2

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
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

fun NPC.turn(entity: Entity) {
    val delta = entity.tile.delta(tile)
    if (delta != Delta.EMPTY) {
        turn(delta.x, delta.y)
    }
}

fun NPC.turn(direction: Direction) = turn(direction.delta.x, direction.delta.y)

fun NPC.turn(deltaX: Int = 0, deltaY: Int = -1) {
    val turn = getTurn()
    turn.x = tile.x
    turn.y = tile.y
    turn.directionX = deltaX
    turn.directionY = deltaY
    turn.direction = getFaceDirection(deltaX, deltaY)
    flagTurn()
}

fun getFaceDirection(xOffset: Int, yOffset: Int): Int {
    return (atan2(xOffset * -1.0, yOffset * -1.0) * 2607.5945876176133).toInt() and 0x3fff
}
