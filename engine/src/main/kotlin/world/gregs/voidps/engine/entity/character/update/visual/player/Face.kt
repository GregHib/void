package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Direction.Companion.cardinal
import world.gregs.voidps.engine.entity.Direction.Companion.ordinal
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.add
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.visual.VisualMask.FACE_DIRECTION_MASK
import world.gregs.voidps.network.visual.update.player.Face

fun Face.getDirection(): Direction {
    val dx = deltaX.coerceIn(-1, 1)
    val dy = deltaY.coerceIn(-1, 1)
    return Direction.of(dx, dy)
}

fun Player.flagFace() = visuals.flag(FACE_DIRECTION_MASK)

fun Character.face(direction: Direction, update: Boolean = true) {
    if (this is NPC) {
        turn(direction, update)
    } else if (this is Player) {
        face(direction.delta.x, direction.delta.y, update)
    }
}

fun Player.face(tile: Tile, update: Boolean = true) {
    val delta = tile.delta(this.tile)
    if (delta != Delta.EMPTY) {
        face(delta.x, delta.y, update)
    }
}

fun Character.face(entity: Entity, update: Boolean = true) {
    if (this is NPC) {
        turn(entity, update)
        return
    }
    this as Player
    val tile = when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.size, this.tile)
        else -> entity.tile
    }
    var delta = tile.delta(this.tile)
    if (delta != Delta.EMPTY) {
        face(delta.x, delta.y, update)
    } else if (entity is GameObject) {
        when (entity.type) {
            0, 4, 5, 6, 7, 8, 9 -> face(cardinal[(entity.rotation + 3) and 0x3], update)
            1, 2, 3 -> face(ordinal[entity.rotation], update)
            else -> {
                delta = tile.add(size).delta(entity.tile.add(entity.size))
                if (delta != Delta.EMPTY) {
                    face(delta.x, delta.y, update)
                }
            }
        }
    }
}

fun Player.face(deltaX: Int = 0, deltaY: Int = -1, update: Boolean = true) {
    val face = visuals.face
    face.deltaX = deltaX
    face.deltaY = deltaY
    if (update) {
        flagFace()
    }
}

var Player.direction: Direction
    get() = visuals.face.getDirection()
    set(value) = face(value)