package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.network.visual.VisualMask.TURN_MASK
import kotlin.math.atan2

fun NPC.flagTurn() = visuals.flag(TURN_MASK)

fun NPC.turn(entity: Entity, update: Boolean = true) {
    val tile = when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.size, this.tile)
        else -> entity.tile
    }
    val delta = tile.delta(tile)
    if (delta != Delta.EMPTY) {
        turn(delta.x, delta.y, update)
    }
}

fun NPC.turn(direction: Direction, update: Boolean = true) = turn(direction.delta.x, direction.delta.y, update)

fun NPC.turn(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val turn = visuals.turn
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
