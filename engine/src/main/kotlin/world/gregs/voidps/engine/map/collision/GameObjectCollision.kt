package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

class GameObjectCollision(val collisions: Collisions) {

    fun modifyCollision(gameObject: GameObject, changeType: Int) {
        modifyCollision(gameObject.def, gameObject.tile, gameObject.type, gameObject.rotation, changeType)
    }

    fun modifyCollision(def: ObjectDefinition, tile: Tile, type: Int, rotation: Int, changeType: Int) {
        if (def.solid == 0) {
            return
        }

        when (type) {
            in 0..3 -> modifyWall(def, tile, type, rotation, changeType)
            in 9..21 -> modifyObject(def, tile, rotation, changeType)
            22 -> {
                if (def.solid == 1) {
                    modifyMask(tile.x, tile.y, tile.plane, CollisionFlag.FLOOR_DECO, changeType)
                }
            }
        }
    }

    private fun modifyObject(def: ObjectDefinition, tile: Tile, rotation: Int, changeType: Int) {
        var mask = CollisionFlag.LAND

        if (def.blocksSky) {//solid
            mask = mask or CollisionFlag.SKY
        }

        if (def.ignoreOnRoute) {//not alt
            mask = mask or CollisionFlag.IGNORED
        }

        val width = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
        val height = if (rotation and 0x1 == 1) def.sizeX else def.sizeY

        for (offsetX in 0 until width) {
            for (offsetY in 0 until height) {
                modifyMask(tile.x + offsetX, tile.y + offsetY, tile.plane, mask, changeType)
            }
        }
    }


    private fun modifyWall(def: ObjectDefinition, tile: Tile, type: Int, rotation: Int, changeType: Int) {
        modifyWall(tile, type, rotation, 0, changeType)
        if (def.blocksSky) {
            modifyWall(tile, type, rotation, 1, changeType)
        }
        if (def.ignoreOnRoute) {
            modifyWall(tile, type, rotation, 2, changeType)
        }
    }

    /**
     * Wall types:
     * 0 - ║ External wall (vertical or horizontal)
     * 1 - ╔ External corner (flat/missing)
     * 2 - ╝ Internal corner
     * 3 - ╔ External corner (regular)
     */
    private fun modifyWall(tile: Tile, type: Int, rotation: Int, motion: Int, changeType: Int) {
        var t = tile

        // Internal corners
        if (type == 2) {
            // Mask both cardinal directions
            val or = when (Direction.ordinal[rotation and 0x3]) {
                Direction.NORTH_WEST -> CollisionFlag.NORTH_OR_WEST
                Direction.NORTH_EAST -> CollisionFlag.NORTH_OR_EAST
                Direction.SOUTH_EAST -> CollisionFlag.SOUTH_OR_EAST
                Direction.SOUTH_WEST -> CollisionFlag.SOUTH_OR_WEST
                else -> 0
            }
            modifyMask(tile.x, tile.y, tile.plane, applyMotion(or, motion), changeType)
            t = t.add(Direction.cardinal[(rotation + 3) and 0x3].delta)
        }

        // Mask one wall side
        var direction = when (type) {
            0 -> Direction.cardinal[(rotation + 3) and 0x3]
            2 -> Direction.cardinal[(rotation + 1) and 0x3]
            else -> Direction.ordinal[rotation and 0x3]
        }
        modifyMask(t.x, t.y, t.plane, direction.flag(motion), changeType)

        // Mask other wall side
        t = if (type == 2) {
            tile.add(Direction.cardinal[rotation and 0x3].delta)
        } else {
            tile.add(direction.delta)
        }
        direction = when (type) {
            2 -> Direction.cardinal[(rotation + 2) and 0x3]
            else -> direction.inverse()
        }
        modifyMask(t.x, t.y, t.plane, direction.flag(motion), changeType)
    }

    private fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, changeType: Any) {
        when (changeType) {
            ADD_MASK -> collisions.add(x, y, plane, mask)
            REMOVE_MASK -> collisions.remove(x, y, plane, mask)
            SET_MASK -> collisions[x, y, plane] = mask
        }
    }

    private fun applyMotion(mask: Int, motion: Int): Int {
        return when (motion) {
            1 -> mask shl 9
            2 -> mask shl 22
            else -> mask
        }
    }

    fun Direction.flag(motion: Int) = applyMotion(flag(), motion)

    companion object {
        const val ADD_MASK = 0
        const val REMOVE_MASK = 1
        const val SET_MASK = 2
    }
}