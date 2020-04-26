package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Turn(
    var x: Int = 0,
    var y: Int = 0,
    var directionX: Int = 0,
    var directionY: Int = 0
) : Visual

fun NPC.getTurn() = visuals.getOrPut(Turn::class) { Turn() }

fun NPC.flagTurn() = visuals.flag(0x8)

fun NPC.turn(deltaX: Int = 0, deltaY: Int = -1) {
    val turn = getTurn()
    turn.x = tile.x
    turn.y = tile.y
    turn.directionX = deltaX
    turn.directionY = deltaY
    flagTurn()
}