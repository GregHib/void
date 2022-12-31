package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectType
import world.gregs.voidps.engine.map.Tile

class GameObjectCollision(
    private val collisions: Collisions
) {

    fun modifyCollision(gameObject: GameObject, add: Boolean) {
        modifyCollision(gameObject.def, gameObject.tile, gameObject.type, gameObject.rotation, add)
    }

    fun modifyCollision(def: ObjectDefinition, tile: Tile, type: Int, rotation: Int, add: Boolean) {
        if (def.solid == 0) {
            return
        }
        val blockSky = def.blocksSky
        val blockRoute = def.ignoreOnRoute

        when (type) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(tile, blockSky, blockRoute, Direction.cardinal[(rotation + 3) and 0x3], add)
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(tile, blockSky, blockRoute, Direction.ordinal[rotation], add)
            ObjectType.WALL_CORNER -> modifyWallCorner(tile, blockSky, blockRoute, Direction.ordinal[rotation], add)
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, tile, rotation, blockSky, blockRoute, add)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(tile.x, tile.y, tile.plane, blockSky, blockRoute, add)
        }
    }

    private fun modifyWall(tile: Tile, blockSky: Boolean, blockRoute: Boolean, direction: Direction, add: Boolean) {
        modifyTile(tile.x, tile.y, tile.plane, blockSky, blockRoute, direction, add)
        modifyTile(tile.x + direction.delta.x, tile.y + direction.delta.y, tile.plane, blockSky, blockRoute, direction.inverse(), add)
    }

    private fun modifyWallCorner(tile: Tile, blockSky: Boolean, blockRoute: Boolean, direction: Direction, add: Boolean) {
        modifyWall(tile, blockSky, blockRoute, direction.vertical(), add)
        modifyWall(tile, blockSky, blockRoute, direction.horizontal(), add)
    }

    private fun modifyObject(def: ObjectDefinition, tile: Tile, rotation: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                modifyCardinal(tile.x + dx, tile.y + dy, tile.plane, blockSky, blockRoute, add)
            }
        }
    }

    private fun modifyCardinal(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        modifyTile(x, y, plane, blockSky, blockRoute, Direction.NORTH, add)
        modifyTile(x, y, plane, blockSky, blockRoute, Direction.EAST, add)
        modifyTile(x, y, plane, blockSky, blockRoute, Direction.SOUTH, add)
        modifyTile(x, y, plane, blockSky, blockRoute, Direction.WEST, add)
    }

    private fun modifyTile(x: Int, y: Int, plane: Int, blockSky: Boolean, blockRoute: Boolean, direction: Direction, add: Boolean) {
        if (direction == Direction.NONE) {
            return
        }
        val orientation = direction.ordinal
        if (!blockRoute) {
            modifyMask(x, y, plane, CollisionFlags.routeFlags[orientation].bit, add)
        }
        if (blockSky) {
            modifyMask(x, y, plane, CollisionFlags.projectileFlags[orientation].bit, add)
        }
        modifyMask(x, y, plane, CollisionFlags.wallFlags[orientation].bit, add)
    }

    private fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, add: Boolean) {
        if (add) {
            collisions.add(x, y, plane, mask)
        } else {
            collisions.remove(x, y, plane, mask)
        }
    }
}