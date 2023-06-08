package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectType
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
        modifyTile(x + deltaX[direction], y + deltaY[direction], plane, blockSky, blockRoute, inverse[direction], add)
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


    fun modify(def: ObjectDefinition, zone: Int, tile: Int, info: Int) {
        if (def.solid == 0) {
            return
        }
        val rotation = ZoneObject.infoRotation(info)
        when (ZoneObject.infoType(info)) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(zone, tile, def.block, cardinal[(rotation + 3) and 0x3])
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(zone, tile, def.block, ordinal[rotation])
            ObjectType.WALL_CORNER -> modifyWallCorner(zone, tile, def.block, ordinal[rotation])
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, zone, tile, def.block, rotation)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(zone, tile, def.block)
        }
    }

    var count = 0
    private fun modifyWall(zone: Int, tile: Int, block: Int, direction: Int) {
        modifyTile(zone, tile, block or direction)
        modifyTile(zone, tile, deltaX[direction], deltaY[direction], block or inverse[direction])
    }

    private fun modifyWallCorner(zone: Int, tile: Int, block: Int, direction: Int) {
        modifyWall(zone, tile, block, vertical[direction])
        modifyWall(zone, tile, block, horizontal[direction])
    }

    private fun modifyObject(def: ObjectDefinition, zone: Int, tile: Int, block: Int, rotation: Int) {
        if (def.sizeX == 1 && def.sizeY == 1) {
            modifyCardinal(zone, tile, block or rotation)
            return
        }
        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                modifyCardinal(zone, tile, dx, dy, block)
            }
        }
    }

    private fun modifyCardinal(zone: Int, tile: Int, block: Int) {
        modifyTile(zone, tile, block or 1) // North
        modifyTile(zone, tile, block or 3) // East
        modifyTile(zone, tile, block or 5) // South
        modifyTile(zone, tile, block or 7) // West
    }

    private fun modifyCardinal(zone: Int, tile: Int, dx: Int, dy: Int, block: Int) {
        modifyTile(zone, tile, dx, dy, block or 1) // North
        modifyTile(zone, tile, dx, dy, block or 3) // East
        modifyTile(zone, tile, dx, dy, block or 5) // South
        modifyTile(zone, tile, dx, dy, block or 7) // West
    }

    private fun modifyTile(zone: Int, tile: Int, x: Int, y: Int, block: Int) {
        val adjustedX = tile + x and 0x7
        val adjustedY = (tile shr 3) + y
        val newTile = adjustedX or (adjustedY shl 3) and 0x3f
        val tileX = ZoneObject.tileX(tile)
        val tileY = ZoneObject.tileY(tile)
        val remX = tileX + x shr 3
        val remY = tileY + y shr 3
        val newZone = zone + (remX or (remY shl 12))
        modifyTile(newZone, newTile, block)
    }

    private fun modifyTile(zone: Int, tile: Int, block: Int) {
        val flags = collisions.flags[zone] ?: return
        flags[tile] = flags[tile] or CollisionFlags.array[block]
    }

    companion object {


        fun addTile(tile: Int, x: Int, y: Int): Int {
            val adjustedX = tile + x and 0x7
            val adjustedY = (tile shr 3) + y
            return adjustedX or (adjustedY shl 3) and 0x3f
        }


        fun addZone(zone: Int, tile: Int, x: Int, y: Int): Int {
            val x = (tile and 0x7 + x).mod(0x7ff)
            val y = (tile shr 3 + y).mod(0x7ff)
            val adjustedX = zone + x and 0x7ff
            val adjustedY = (zone shr 12) + y
            return adjustedX or (adjustedY shl 12)
        }

        fun zoneIndex(zoneX: Int, zoneY: Int, level: Int): Int = zoneX or (zoneY shl 11) or (level shl 22)
        fun tileX(zone: Int) = zone shl 3 and 0x7ff
        fun tileY(zone: Int) = (zone shr 11 shl 3) and 0x7ff
        fun zoneX(zone: Int) = zone and 0x7ff
        fun zoneY(zone: Int) = (zone shr 11) and 0x7ff
        fun level(zone: Int) = zone shr 22 and 0x3

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