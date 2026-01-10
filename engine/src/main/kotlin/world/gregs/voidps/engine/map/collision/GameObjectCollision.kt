package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Direction

abstract class GameObjectCollision {
    fun modify(obj: GameObject) {
        modify(obj.def, obj.x, obj.y, obj.level, obj.shape, obj.rotation)
    }

    fun modify(def: ObjectDefinition, x: Int, y: Int, level: Int, shape: Int, rotation: Int) {
        if (def.solid == 0) {
            return
        }
        when (shape) {
            ObjectShape.WALL_STRAIGHT -> modifyWall(x, y, level, def.block, cardinal[(rotation + 3) and 0x3])
            ObjectShape.WALL_DIAGONAL_CORNER, ObjectShape.WALL_SQUARE_CORNER -> modifyWall(x, y, level, def.block, ordinal[rotation])
            ObjectShape.WALL_CORNER -> modifyWallCorner(x, y, level, def.block, ordinal[rotation])
            in ObjectShape.WALL_DIAGONAL until ObjectShape.GROUND_DECOR -> modifyObject(def, x, y, level, rotation, def.block)
            ObjectShape.GROUND_DECOR -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(x, y, level, def.block)
        }
    }

    private fun modifyWall(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        modifyTile(x, y, level, block, direction)
        modifyTile(x + deltaX[direction], y + deltaY[direction], level, block, inverse[direction])
    }

    private fun modifyWallCorner(x: Int, y: Int, level: Int, block: Int, direction: Int) {
        modifyWall(x, y, level, block, vertical[direction])
        modifyWall(x, y, level, block, horizontal[direction])
    }

    private fun modifyObject(def: ObjectDefinition, x: Int, y: Int, level: Int, rotation: Int, block: Int) {
        when (def.sizeX) {
            1 if def.sizeY == 1 -> modifyCardinal(x, y, level, block)
            2 if def.sizeY == 2 -> {
                modifyCardinal(x, y, level, block)
                modifyCardinal(x + 1, y, level, block)
                modifyCardinal(x, y + 1, level, block)
                modifyCardinal(x + 1, y + 1, level, block)
            }
            3 if def.sizeY == 3 -> {
                modifyCardinal(x, y, level, block)
                modifyCardinal(x, y + 1, level, block)
                modifyCardinal(x, y + 2, level, block)
                modifyCardinal(x + 1, y, level, block)
                modifyCardinal(x + 1, y + 1, level, block)
                modifyCardinal(x + 1, y + 2, level, block)
                modifyCardinal(x + 2, y, level, block)
                modifyCardinal(x + 2, y + 1, level, block)
                modifyCardinal(x + 2, y + 2, level, block)
            }
            else -> {
                val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
                val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
                when (width) {
                    1 if height == 2 -> {
                        modifyCardinal(x, y, level, block)
                        modifyCardinal(x, y + 1, level, block)
                    }
                    2 if height == 1 -> {
                        modifyCardinal(x, y, level, block)
                        modifyCardinal(x + 1, y, level, block)
                    }
                    else -> for (dx in 0 until width) {
                        for (dy in 0 until height) {
                            modifyCardinal(x + dx, y + dy, level, block)
                        }
                    }
                }
            }
        }
    }

    private fun modifyCardinal(x: Int, y: Int, level: Int, block: Int) {
        modifyTile(x, y, level, block, 1)
        modifyTile(x, y, level, block, 3)
        modifyTile(x, y, level, block, 5)
        modifyTile(x, y, level, block, 7)
    }

    abstract fun modifyTile(x: Int, y: Int, level: Int, block: Int, direction: Int)

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
