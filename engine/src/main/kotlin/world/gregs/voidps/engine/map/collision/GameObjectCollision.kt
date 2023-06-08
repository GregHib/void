package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectType
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
        val x = obj.x + (Chunk.indexX(chunk) shl 3)
        val y = obj.y + (Chunk.indexY(chunk) shl 3)
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
            return
        }
        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                modifyCardinal(x + dx, y + dy, plane, block, add)
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
        val flags = collisions.flags[zoneIndex(x, y, plane)] ?: return
        val tile = tileIndex(x, y)
        var mask = CollisionFlags.wallFlags[direction]
        if (block and 8 == 8) {
            mask = mask or CollisionFlags.routeFlags[direction]
        }
        if (block and 4 == 4) {
            mask = mask or CollisionFlags.projectileFlags[direction]
        }
        if (add) {
            flags[tile] = flags[tile] or mask
        } else {
            flags[tile] = flags[tile] and mask.inv()
        }
    }

    // For performance reasons
    companion object {
        fun tileIndex(x: Int, z: Int): Int = (x and 0x7) or ((z and 0x7) shl 3)

        fun zoneIndex(x: Int, z: Int, level: Int): Int = ((x shr 3) and 0x7FF) or
                (((z shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)

        private val inverse = Direction.all.map { it.inverse().ordinal }.toIntArray()

        private val deltaX = Direction.all.map { it.delta.x }.toIntArray()
        private val deltaY = Direction.all.map { it.delta.y }.toIntArray()

        private val cardinal = Direction.cardinal.map(Direction::ordinal).toIntArray()
        private val ordinal = Direction.ordinal.map(Direction::ordinal).toIntArray()

        private val vertical = Direction.all.map { it.vertical().ordinal }.toIntArray()
        private val horizontal = Direction.all.map { it.horizontal().ordinal }.toIntArray()
    }
}