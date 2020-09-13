package rs.dusk.engine.entity.character.update.visual.player

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.entity.character.update.Visual
import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Face(var deltaX: Int = 0, var deltaY: Int = -1) : Visual {
    fun getDirection(): Direction {
        val dx = deltaX.coerceIn(-1, 1)
        val dy = deltaY.coerceIn(-1, 1)
        return Direction.of(dx, dy)
    }
}

const val FACE_DIRECTION_MASK = 0x20

fun Player.flagFace() = visuals.flag(FACE_DIRECTION_MASK)

fun Player.getFace() = visuals.getOrPut(FACE_DIRECTION_MASK) { Face() }

fun PlayerEvent.face(direction: Direction) = face(direction.delta.x, direction.delta.y)

fun PlayerEvent.face(deltaX: Int = 0, deltaY: Int = -1) = player.face(deltaX, deltaY)

fun Player.face(direction: Direction) = face(direction.delta.x, direction.delta.y)

fun Player.face(entity: Entity) {
    val delta = entity.tile.delta(tile)
    if(delta != Tile.EMPTY) {
        face(delta.x, delta.y)
    }
}

fun Player.face(deltaX: Int = 0, deltaY: Int = -1) {
    val face = getFace()
    face.deltaX = deltaX
    face.deltaY = deltaY
    flagFace()
}