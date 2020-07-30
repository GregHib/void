import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Direction.Companion.cardinal
import rs.dusk.engine.entity.Direction.Companion.ordinal
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.map.collision.*
import rs.dusk.engine.map.collision.CollisionFlag.IGNORED
import rs.dusk.engine.map.collision.CollisionFlag.LAND
import rs.dusk.engine.map.collision.CollisionFlag.NORTH_OR_EAST
import rs.dusk.engine.map.collision.CollisionFlag.NORTH_OR_WEST
import rs.dusk.engine.map.collision.CollisionFlag.SKY
import rs.dusk.engine.map.collision.CollisionFlag.SOUTH_OR_EAST
import rs.dusk.engine.map.collision.CollisionFlag.SOUTH_OR_WEST
import rs.dusk.utility.inject

val collisions: Collisions by inject()

Registered priority 9 where { entity is GameObject } then {
    modifyCollision(entity as GameObject, ADD_MASK)
}

Unregistered priority 9 where { entity is GameObject } then {
    modifyCollision(entity as GameObject, REMOVE_MASK)
}

fun modifyCollision(gameObject: GameObject, changeType: Int) {
    if (gameObject.def.solid == 0) {
        return
    }

    when (gameObject.type) {
        in 0..3 -> modifyWall(gameObject, changeType)
        in 9..21 -> modifyObject(gameObject, changeType)
        22 -> {
            if (gameObject.def.solid == 1) {
                modifyMask(gameObject.tile.x, gameObject.tile.y, gameObject.tile.plane, CollisionFlag.FLOOR_DECO, changeType)
            }
        }
    }
}

fun modifyObject(gameObject: GameObject, changeType: Int) {
    var mask = LAND

    if (gameObject.def.blocksSky) {//solid
        mask = mask or SKY
    }

    if (!gameObject.def.ignoreOnRoute) {//not alt
        mask = mask or IGNORED
    }

    var width = gameObject.size.width
    var height = gameObject.size.height

    if (gameObject.rotation and 0x1 == 1) {
        width = gameObject.size.height
        height = gameObject.size.width
    }

    for (offsetX in 0 until width) {
        for (offsetY in 0 until height) {
            modifyMask(gameObject.tile.x + offsetX, gameObject.tile.y + offsetY, gameObject.tile.plane, mask, changeType)
        }
    }
}


fun modifyWall(gameObject: GameObject, changeType: Int) {
    modifyWall(gameObject, 0, changeType)
    if (gameObject.def.blocksSky) {
        modifyWall(gameObject, 1, changeType)
    }
    if (!gameObject.def.ignoreOnRoute) {
        modifyWall(gameObject, 2, changeType)
    }
}

/**
 * Wall types:
 * 0 - ║ External wall (vertical or horizontal)
 * 1 - ╔ External corner (flat/missing)
 * 2 - ╝ Internal corner
 * 3 - ╔ External corner (regular)
 */
fun modifyWall(gameObject: GameObject, motion: Int, changeType: Int) {
    val rotation = gameObject.rotation
    val type = gameObject.type
    var tile = gameObject.tile

    // Internal corners
    if (type == 2) {
        // Mask both cardinal directions
        val or = when (ordinal[rotation and 0x3]) {
            Direction.NORTH_WEST -> NORTH_OR_WEST
            Direction.NORTH_EAST -> NORTH_OR_EAST
            Direction.SOUTH_EAST -> SOUTH_OR_EAST
            Direction.SOUTH_WEST -> SOUTH_OR_WEST
            else -> 0
        }
        modifyMask(gameObject.tile.x, gameObject.tile.y, gameObject.tile.plane, applyMotion(or, motion), changeType)
        tile = tile.add(cardinal[(rotation + 3) and 0x3].delta)
    }

    // Mask one wall side
    var direction = when (type) {
        0 -> cardinal[(rotation + 3) and 0x3]
        2 -> cardinal[(rotation + 1) and 0x3]
        else -> ordinal[rotation and 0x3]
    }
    modifyMask(tile.x, tile.y, tile.plane, direction.flag(motion), changeType)

    // Mask other wall side
    tile = if (type == 2) {
        gameObject.tile.add(cardinal[rotation and 0x3].delta)
    } else {
        gameObject.tile.add(direction.delta)
    }
    direction = when (type) {
        2 -> cardinal[(rotation + 2) and 0x3]
        else -> direction.inverse()
    }
    modifyMask(tile.x, tile.y, tile.plane, direction.flag(motion), changeType)
}

val ADD_MASK = 0
val REMOVE_MASK = 1
val SET_MASK = 2

fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, changeType: Any) {
    when (changeType) {
        ADD_MASK -> collisions.add(x, y, plane, mask)
        REMOVE_MASK -> collisions.remove(x, y, plane, mask)
        SET_MASK -> collisions[x, y, plane] = mask
    }
}

fun applyMotion(mask: Int, motion: Int): Int {
    return when (motion) {
        1 -> mask shl 9
        2 -> mask shl 22
        else -> mask
    }
}

fun Direction.flag(motion: Int) = applyMotion(flag(), motion)