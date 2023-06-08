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

    fun modify(def: ObjectDefinition, zone: Int, tile: Int, info: Int, add: Boolean) {
        if (def.solid == 0) {
            return
        }
        val blockSky = def.blocksSky
        val blockRoute = def.ignoreOnRoute
        val rotation = ZoneObject.infoRotation(info)
        when (ZoneObject.infoType(info)) {
            ObjectType.LENGTHWISE_WALL -> modifyWall(zone, tile, blockSky, blockRoute, cardinal[(rotation + 3) and 0x3], add)
            ObjectType.TRIANGULAR_CORNER, ObjectType.RECTANGULAR_CORNER -> modifyWall(zone, tile, blockSky, blockRoute, ordinal[rotation], add)
            ObjectType.WALL_CORNER -> modifyWallCorner(zone, tile, blockSky, blockRoute, ordinal[rotation], add)
            in ObjectType.DIAGONAL_WALL until ObjectType.FLOOR_DECORATION -> modifyObject(def, zone, tile, rotation, blockSky, blockRoute, add)
            ObjectType.FLOOR_DECORATION -> if (def.interactive == 1 && def.solid == 1) modifyCardinal(zone, tile, blockSky, blockRoute, add)
        }
    }

    private fun modifyWall(zone: Int, tile: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        modifyTile(zone, tile, blockSky, blockRoute, direction, add)
        modifyTile(zoneX(zone) + ZoneObject.tileX(tile) + deltaX[direction], zoneY(zone) + ZoneObject.tileY(tile) + deltaY[direction], level(zone), blockSky, blockRoute, inverse[direction], add)
    }

    private fun modifyWallCorner(zone: Int, tile: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        modifyWall(zone, tile, blockSky, blockRoute, vertical[direction], add)
        modifyWall(zone, tile, blockSky, blockRoute, horizontal[direction], add)
    }

    private fun modifyObject(def: ObjectDefinition, zone: Int, tile: Int, rotation: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
        val x = zoneX(zone) + ZoneObject.tileX(tile)
        val y = zoneY(zone) + ZoneObject.tileY(tile)
        val plane = level(zone)
        for (dx in 0 until width) {
            for (dy in 0 until height) {
                modifyCardinal(x + dx, y + dy, plane, blockSky, blockRoute, add)
            }
        }
    }

    private fun modifyCardinal(zone: Int, tile: Int, blockSky: Boolean, blockRoute: Boolean, add: Boolean) {
        modifyTile(zone, tile, blockSky, blockRoute, 1, add)
        modifyTile(zone, tile, blockSky, blockRoute, 3, add)
        modifyTile(zone, tile, blockSky, blockRoute, 5, add)
        modifyTile(zone, tile, blockSky, blockRoute, 7, add)
    }

    private fun modifyTile(zone: Int, tile: Int, blockSky: Boolean, blockRoute: Boolean, direction: Int, add: Boolean) {
        var mask = CollisionFlags.wallFlags[direction]
        if (!blockRoute) {
            mask = mask or CollisionFlags.routeFlags[direction]
        }
        if (blockSky) {
            mask = mask or CollisionFlags.projectileFlags[direction]
        }
        modifyMask(zone, tile, mask, add)
    }

    private fun modifyMask(zone: Int, tile: Int, mask: Int, add: Boolean) {
        if (add) {
            val current = collisions.flags[zone]?.get(tile) ?: 0
            val existing = allocateIfAbsent(zone)
            existing[tile] = current or mask
        } else {
            val current = collisions.flags[zone]?.get(tile) ?: -1
            val existing = allocateIfAbsent(zone)
            existing[tile] = current and mask.inv()
        }
    }

    private fun allocateIfAbsent(zone: Int): IntArray {
        val existing = collisions.flags[zone]
        if (existing != null) return existing
        val flags = IntArray(64)
        collisions.flags[zone] = flags
        return flags
    }

    companion object {

        fun zoneIndex(zoneX: Int, zoneY: Int, level: Int): Int = zoneX or (zoneY shl 11) or (level shl 22)
        fun zoneX(zone: Int) = (zone) and 0x7ff
        fun zoneY(zone: Int) = (zone shr 11) shl 3 and 0x7ff
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