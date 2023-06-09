package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectType
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.file.ZoneObject

class GameObjectCollision(
    private val collisions: Collisions
) {
    fun modify(obj: GameObject, add: Boolean) {
        modify(obj.def, obj.x, obj.y, obj.plane, obj.type, obj.rotation, add)
    }

    fun modify(def: ObjectDefinition, x: Int, y: Int, plane: Int, type: Int, rotation: Int, add: Boolean) {
        if (def.solid == 0) {
            return
        }
        when (type) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(x, y, plane, def.block, cardinal[(rotation + 3) and 0x3], add)
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(x, y, plane, def.block, ordinal[rotation], add)
            ObjectType.WALL_CORNER -> modifyWallCorner(x, y, plane, def.block, ordinal[rotation], add)
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, x, y, plane, rotation, def.block, add)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(x, y, plane, def.block, add)
        }
    }

    fun modify(obj: ZoneObject, chunk: Int, def: ObjectDefinition) {
        if (def.solid == 0) {
            return
        }
        val x = obj.x + (Chunk.x(chunk) shl 3)
        val y = obj.y + (Chunk.y(chunk) shl 3)
        val plane = obj.plane
        val rotation = obj.rotation
        when (obj.type) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(x, y, plane, def.block, cardinal[(rotation + 3) and 0x3], true)
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(x, y, plane, def.block, ordinal[rotation], true)
            ObjectType.WALL_CORNER -> modifyWallCorner(x, y, plane, def.block, ordinal[rotation], true)
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, x, y, plane, rotation, def.block, true)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(x, y, plane, def.block, true)
        }
    }

    private fun modifyWall(x: Int, y: Int, plane: Int, block: Int, direction: Int, add: Boolean) {
        modifyTile(x, y, plane, block, direction, add)
        modifyTile(x + deltaX[direction], y + deltaY[direction], plane, block, inverse[direction], add)
    }

    private fun modifyWallCorner(x: Int, y: Int, plane: Int, block: Int, direction: Int, add: Boolean) {
        modifyWall(x, y, plane, block, vertical[direction], add)
        modifyWall(x, y, plane, block, horizontal[direction], add)
    }

    private fun modifyObject(def: ObjectDefinition, x: Int, y: Int, plane: Int, rotation: Int, block: Int, add: Boolean) {
        if (def.sizeX == 1 && def.sizeY == 1) {
            modifyCardinal(x, y, plane, block, add)
        } else if (def.sizeX == 2 && def.sizeY == 2) {
            modifyCardinal(x, y, plane, block, add)
            modifyCardinal(x + 1, y, plane, block, add)
            modifyCardinal(x, y + 1, plane, block, add)
            modifyCardinal(x + 1, y + 1, plane, block, add)
        } else if (def.sizeX == 3 && def.sizeY == 3) {
            modifyCardinal(x, y + 1, plane, block, add)
            modifyCardinal(x, y + 2, plane, block, add)
            modifyCardinal(x + 1, y, plane, block, add)
            modifyCardinal(x + 1, y + 1, plane, block, add)
            modifyCardinal(x + 2, y, plane, block, add)
            modifyCardinal(x + 2, y + 2, plane, block, add)
        } else {
            val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
            val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
            if (width == 1 && height == 2) {
                modifyCardinal(x, y, plane, block, add)
                modifyCardinal(x, y + 1, plane, block, add)
            } else if (width == 2 && height == 1) {
                modifyCardinal(x, y, plane, block, add)
                modifyCardinal(x + 1, y, plane, block, add)
            } else {
                for (dx in 0 until width) {
                    for (dy in 0 until height) {
                        modifyCardinal(x + dx, y + dy, plane, block, add)
                    }
                }
            }
        }
    }

    private fun modifyCardinal(x: Int, y: Int, plane: Int, block: Int, add: Boolean) {
        modifyTile(x, y, plane, block, 1, add)
        modifyTile(x, y, plane, block, 3, add)
        modifyTile(x, y, plane, block, 5, add)
        modifyTile(x, y, plane, block, 7, add)
    }

    private fun modifyTile(x: Int, y: Int, plane: Int, block: Int, direction: Int, add: Boolean) {
        val flags = collisions.flags[Chunk.tileIndex(x, y, plane)] ?: return
        if (add) {
            flags[Tile.index(x, y)] = flags[Tile.index(x, y)] or CollisionFlags.blocked[direction or block]
        } else {
            flags[Tile.index(x, y)] = flags[Tile.index(x, y)] and CollisionFlags.inverse[direction or block]
        }
    }

    companion object {
        // For performance reasons
        private val inverse = Direction.all.map { it.inverse().ordinal }.toIntArray()

        private val deltaX = Direction.all.map { it.delta.x }.toIntArray()
        private val deltaY = Direction.all.map { it.delta.y }.toIntArray()

        private val cardinal = Direction.cardinal.map(Direction::ordinal).toIntArray()
        private val ordinal = Direction.ordinal.map(Direction::ordinal).toIntArray()

        private val vertical = Direction.all.map { it.vertical().ordinal }.toIntArray()
        private val horizontal = Direction.all.map { it.horizontal().ordinal }.toIntArray()
    }
}