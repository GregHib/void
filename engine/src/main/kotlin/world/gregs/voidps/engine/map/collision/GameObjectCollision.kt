package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class GameObjectCollision(
    private val collisions: Collisions
) {
    fun modify(obj: GameObject, add: Boolean) {
        modify(obj.def, obj.x, obj.y, obj.level, obj.shape, obj.rotation, add)
    }

    fun modify(def: ObjectDefinition, x: Int, y: Int, level: Int, shape: Int, rotation: Int, add: Boolean) {
        if (def.solid == 0) {
            return
        }
        when (shape) {
            ObjectShape.WALL_STRAIGHT -> modifyWall(x, y, level, def.block, cardinal[(rotation + 3) and 0x3], add)
            ObjectShape.WALL_DIAGONAL_CORNER, ObjectShape.WALL_SQUARE_CORNER -> modifyWall(x, y, level, def.block, ordinal[rotation], add)
            ObjectShape.WALL_CORNER -> modifyWallCorner(x, y, level, def.block, ordinal[rotation], add)
            in ObjectShape.WALL_DIAGONAL until ObjectShape.GROUND_DECOR -> modifyObject(def, x, y, level, rotation, def.block, add)
            ObjectShape.GROUND_DECOR -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(x, y, level, def.block, add)
        }
    }

    private fun modifyWall(x: Int, y: Int, level: Int, block: Int, direction: Int, add: Boolean) {
        modifyTile(x, y, level, block, direction, add)
        modifyTile(x + deltaX[direction], y + deltaY[direction], level, block, inverse[direction], add)
    }

    private fun modifyWallCorner(x: Int, y: Int, level: Int, block: Int, direction: Int, add: Boolean) {
        modifyWall(x, y, level, block, vertical[direction], add)
        modifyWall(x, y, level, block, horizontal[direction], add)
    }

    private fun modifyObject(def: ObjectDefinition, x: Int, y: Int, level: Int, rotation: Int, block: Int, add: Boolean) {
        if (def.sizeX == 1 && def.sizeY == 1) {
            modifyCardinal(x, y, level, block, add)
        } else if (def.sizeX == 2 && def.sizeY == 2) {
            modifyCardinal(x, y, level, block, add)
            modifyCardinal(x + 1, y, level, block, add)
            modifyCardinal(x, y + 1, level, block, add)
            modifyCardinal(x + 1, y + 1, level, block, add)
        } else if (def.sizeX == 3 && def.sizeY == 3) {
            modifyCardinal(x, y + 1, level, block, add)
            modifyCardinal(x, y + 2, level, block, add)
            modifyCardinal(x + 1, y, level, block, add)
            modifyCardinal(x + 1, y + 1, level, block, add)
            modifyCardinal(x + 2, y, level, block, add)
            modifyCardinal(x + 2, y + 2, level, block, add)
        } else {
            val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
            val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
            if (width == 1 && height == 2) {
                modifyCardinal(x, y, level, block, add)
                modifyCardinal(x, y + 1, level, block, add)
            } else if (width == 2 && height == 1) {
                modifyCardinal(x, y, level, block, add)
                modifyCardinal(x + 1, y, level, block, add)
            } else {
                for (dx in 0 until width) {
                    for (dy in 0 until height) {
                        modifyCardinal(x + dx, y + dy, level, block, add)
                    }
                }
            }
        }
    }

    private fun modifyCardinal(x: Int, y: Int, level: Int, block: Int, add: Boolean) {
        modifyTile(x, y, level, block, 1, add)
        modifyTile(x, y, level, block, 3, add)
        modifyTile(x, y, level, block, 5, add)
        modifyTile(x, y, level, block, 7, add)
    }

    private fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int, add: Boolean) {
        var flags = collisions.flags[Zone.tileIndex(x, y, level)]
        if (flags == null) {
            flags = collisions.allocateIfAbsent(x, y, level)
        }
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