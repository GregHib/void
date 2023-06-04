package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction.*
import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectType
import world.gregs.voidps.engine.map.Tile

class GameObjectCollision(
    private val collisions: Collisions
) {

    fun modifyCollision(gameObject: GameObject, add: Boolean) {
        modify(gameObject.def, gameObject.tile.x, gameObject.tile.y, gameObject.tile.plane, gameObject.type, gameObject.rotation, add)
    }

    fun modify(tile: Tile, obj: GameMapObject, add: Boolean) {
        modify(obj.def, tile.x, tile.y, tile.plane, obj.type, obj.rotation, add)
    }

    fun modify(def: ObjectDefinition, x: Int, y: Int, plane: Int, type: Int, rotation: Int, add: Boolean) {
        if (def.solid == 0) {
            return
        }
        val blockSky = def.blocksSky
        val blockRoute = def.ignoreOnRoute
        when (type) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(x, y, plane, blockSky, blockRoute, cardinal[(rotation + 3) and 0x3], add)
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(x, y, plane, blockSky, blockRoute, ordinal[rotation], add)
            ObjectType.WALL_CORNER -> modifyWallCorner(x, y, plane, blockSky, blockRoute, ordinal[rotation], add)
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, x, y, plane, rotation, blockSky, blockRoute, add)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(x, y, plane, blockSky, blockRoute, add)
        }
    }

    private fun modifyWall(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        modifyTile(x, y, plane, blockSky, blockRoute, direction, add)
        modifyTile(x + deltaX[direction], y + deltaY[direction], plane, blockSky, blockRoute, (direction + 4) and 0x3, add)
    }

    private fun modifyWallCorner(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        modifyWall(x, y, plane, blockSky, blockRoute, vertical[direction], add)
        modifyWall(x, y, plane, blockSky, blockRoute, horizontal[direction], add)
    }

    private fun modifyObject(def: ObjectDefinition, x: Int, y: Int, plane: Int, rotation: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                modifyCardinal(x + dx, y + dy, plane, blockSky, blockRoute, add)
            }
        }
    }

    private fun modifyCardinal(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        modifyTile(x, y, plane, blockSky, blockRoute, 1, add)
        modifyTile(x, y, plane, blockSky, blockRoute, 3, add)
        modifyTile(x, y, plane, blockSky, blockRoute, 5, add)
        modifyTile(x, y, plane, blockSky, blockRoute, 7, add)
    }

    private fun modifyTile(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        var mask = CollisionFlags.wallFlags[direction]
        if (!blockRoute) {
            mask = mask or CollisionFlags.routeFlags[direction]
        }
        if (blockSky) {
            mask = mask or CollisionFlags.projectileFlags[direction]
        }
        modifyMask(x, y, plane, mask, add)
    }

    private fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, add: Boolean) {
        if (add) {
            collisions.add(x, y, plane, mask)
        } else {
            collisions.remove(x, y, plane, mask)
        }
    }

    companion object {
        // For performance reasons
        private val deltaX = intArrayOf(-1, 0, 1, 1, 1, 0, -1, -1)
        private val deltaY = intArrayOf(1, 1, 1, 0, -1, -1, -1, 0)

        private val cardinal = intArrayOf(NORTH.ordinal, SOUTH.ordinal, EAST.ordinal, WEST.ordinal)
        private val ordinal = intArrayOf(NORTH_WEST.ordinal, NORTH_EAST.ordinal, SOUTH_EAST.ordinal, SOUTH_WEST.ordinal)

        private val vertical = intArrayOf(NORTH.ordinal, NORTH.ordinal, NORTH.ordinal, -1, SOUTH.ordinal, SOUTH.ordinal, SOUTH.ordinal, -1)
        private val horizontal = intArrayOf(WEST.ordinal, -1, EAST.ordinal, EAST.ordinal, EAST.ordinal, -1, WEST.ordinal, WEST.ordinal)
    }
}