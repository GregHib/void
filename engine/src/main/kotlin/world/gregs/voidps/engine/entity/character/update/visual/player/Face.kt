package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile

data class Face(var deltaX: Int = 0, var deltaY: Int = -1) : Visual {
    fun getDirection(): Direction {
        val dx = deltaX.coerceIn(-1, 1)
        val dy = deltaY.coerceIn(-1, 1)
        return Direction.of(dx, dy)
    }
}

const val FACE_DIRECTION_MASK = 0x2

fun Player.flagFace() = visuals.flag(FACE_DIRECTION_MASK)

fun Player.getFace() = visuals.getOrPut(FACE_DIRECTION_MASK) { Face() }

fun Player.face(direction: Direction, update: Boolean = true) = face(direction.delta.x, direction.delta.y, update)

fun Player.face(tile: Tile, update: Boolean = true) {
    val delta = tile.delta(this.tile)
    if (delta != Delta.EMPTY) {
        face(delta.x, delta.y, update)
    }
}

fun Player.face(entity: Entity, update: Boolean = true) {
    val tile = when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.size, this.tile)
        else -> entity.tile
    }
    val delta = tile.delta(this.tile)
    if (delta != Delta.EMPTY) {
        face(delta.x, delta.y, update)
    }
}

fun Player.face(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val face = getFace()
    face.deltaX = deltaX
    face.deltaY = deltaY
    if (update) {
        flagFace()
    }
}

var Player.direction: Direction
    get() = getFace().getDirection()
    set(value) = face(value)