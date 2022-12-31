package world.gregs.voidps.engine.map.collision

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals

enum class MoreCollisionFlag(private val bit: Int) {

    PAWN_NORTH_WEST(0),

    PAWN_NORTH(1),

    PAWN_NORTH_EAST(2),

    PAWN_EAST(3),

    PAWN_SOUTH_EAST(4),

    PAWN_SOUTH(5),

    PAWN_SOUTH_WEST(6),

    PAWN_WEST(7),

    PROJECTILE_NORTH_WEST(9),

    PROJECTILE_NORTH(10),

    PROJECTILE_NORTH_EAST(11),

    PROJECTILE_EAST(12),

    PROJECTILE_SOUTH_EAST(13),

    PROJECTILE_SOUTH(14),

    PROJECTILE_SOUTH_WEST(15),

    PROJECTILE_WEST(16);

    fun getBitAsShort(): Short = (1 shl bit).toShort()
    fun getBit() = 1 shl bit

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            for(flag in projectileFlags) {
                println("$flag ${flag.bit} ${flag.getBit()}")
            }

            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_WEST_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_EAST_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_EAST_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_WEST_PROJECTILE_BLOCKER)
            println(org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST_PROJECTILE_BLOCKER)
        }
        val values = enumValues<MoreCollisionFlag>()

        private val pawnFlags = arrayOf(
            PAWN_NORTH_WEST,
            PAWN_NORTH,
            PAWN_NORTH_EAST,
            PAWN_WEST,
            PAWN_EAST,
            PAWN_SOUTH_WEST,
            PAWN_SOUTH,
            PAWN_SOUTH_EAST)

        private val projectileFlags = arrayOf(
            PROJECTILE_NORTH_WEST,
            PROJECTILE_NORTH,
            PROJECTILE_NORTH_EAST,
            PROJECTILE_WEST,
            PROJECTILE_EAST,
            PROJECTILE_SOUTH_WEST,
            PROJECTILE_SOUTH,
            PROJECTILE_SOUTH_EAST)

        fun getFlags(projectiles: Boolean): Array<MoreCollisionFlag> = if (projectiles) projectileFlags() else pawnFlags()

        fun pawnFlags() = pawnFlags

        fun projectileFlags() = projectileFlags
    }
}
data class DirectionFlag(val direction: Direction, val impenetrable: Boolean)

class CollisionUpdate(val type: Int, val flags: Object2ObjectOpenHashMap<Tile, ObjectList<DirectionFlag>>)
class Builder {

    private val flags = Object2ObjectOpenHashMap<Tile, ObjectList<DirectionFlag>>()

    private var type: Int? = null

    fun build(): CollisionUpdate {
        check(type != null) { "Type has not been set." }
        return CollisionUpdate(type!!, flags)
    }

    fun setType(type: Int) {
        check(this.type == null) { "Type has already been set." }
        this.type = type
    }

    fun putTile(tile: Tile, impenetrable: Boolean, vararg directions: Direction) {
        check(directions.isNotEmpty()) { "Directions must not be empty." }
        val flags = flags[tile] ?: ObjectArrayList<DirectionFlag>()
        directions.forEach { dir -> flags.add(DirectionFlag(dir, impenetrable)) }
        this.flags[tile] = flags
    }

    private fun putWall(tile: Tile, impenetrable: Boolean, orientation: Direction) {
        putTile(tile, impenetrable, orientation)
        putTile(tile.step(orientation), impenetrable, orientation.inverse())
    }

    fun Tile.step(direction: Direction, num: Int = 1): Tile = Tile(this.x + (num * direction.delta.x), this.y + (num * direction.delta.y), this.plane)


    fun Direction.getDiagonalComponents(): Array<Direction> = when (this) {
        Direction.NORTH_EAST -> arrayOf(Direction.NORTH, Direction.EAST)
        Direction.NORTH_WEST -> arrayOf(Direction.NORTH, Direction.WEST)
        Direction.SOUTH_EAST -> arrayOf(Direction.SOUTH, Direction.EAST)
        Direction.SOUTH_WEST -> arrayOf(Direction.SOUTH, Direction.WEST)
        else -> throw IllegalArgumentException("Must provide a diagonal direction.")
    }

    private fun putLargeCornerWall(tile: Tile, impenetrable: Boolean, orientation: Direction) {
        val directions = orientation.getDiagonalComponents()
        putTile(tile, impenetrable, *directions)

        directions.forEach { dir ->
            putTile(tile.step(dir), impenetrable, dir.inverse())
        }
    }


    fun putObject(def: ObjectDefinition, tile: Tile, type: Int, rotation: Int) {
        if(tile.equals(3208, 3214, 2)) {
            println("Put object. ${def.id} $type $rotation")
        }
        if (def.solid == 0) {
            return
        }
//        if (!unwalkable(def, type)) {
//            return
//        }

        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        var width = def.sizeX
        var length = def.sizeY
        val impenetrable = def.blocksSky

        if (rotation == 1 || rotation == 3) {
            width = def.sizeY
            length = def.sizeX
        }

        if (type == ObjectType.FLOOR_DECORATION.value) {
            if (def.interactive == 1 && def.solid == 1) {
                putTile(Tile(x, y, plane), impenetrable, *Direction.cardinal.toTypedArray())
            }
        } else if (type >= ObjectType.DIAGONAL_WALL.value && type < ObjectType.FLOOR_DECORATION.value) {
            for (dx in 0 until width) {
                for (dz in 0 until length) {
                    putTile(Tile(x + dx, y + dz, plane), impenetrable, *Direction.cardinal.toTypedArray())
                }
            }
        } else if (type == ObjectType.LENGTHWISE_WALL.value) {
            if(tile.equals(3208, 3214, 2)) {
                println("Put wall. ${def.id} $type $rotation")
            }
            putWall(tile, impenetrable, WNES[rotation])
        } else if (type == ObjectType.TRIANGULAR_CORNER.value || type == ObjectType.RECTANGULAR_CORNER.value) {
            putWall(tile, impenetrable, WNES_DIAGONAL[rotation])
        } else if (type == ObjectType.WALL_CORNER.value) {
            putLargeCornerWall(tile, impenetrable, WNES_DIAGONAL[rotation])
        }
    }

    val WNES = arrayOf(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)
    val WNES_DIAGONAL = arrayOf(Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST)
}

enum class ObjectGroup(val value: Int) {

    /**
     * The wall object group, which may block a tile.
     */
    WALL(0),

    /**
     * The wall decoration object group, which never blocks a tile.
     */
    WALL_DECORATION(1),

    /**
     * The interactable object group, for objects that can be clicked and interacted with.
     */
    INTERACTABLE_OBJECT(2),

    /**
     * The ground decoration object group, which may block a tile.
     */
    GROUND_DECORATION(3);
}

enum class ObjectType(val value: Int, val group: ObjectGroup) {

    /**
     * A wall that is presented lengthwise with respect to the tile.
     */
    LENGTHWISE_WALL(0, ObjectGroup.WALL),

    /**
     * A triangular object positioned in the corner of the tile.
     */
    TRIANGULAR_CORNER(1, ObjectGroup.WALL),

    /**
     * A corner for a wall, where the model is placed on two perpendicular edges of a single tile.
     */
    WALL_CORNER(2, ObjectGroup.WALL),

    /**
     * A rectangular object positioned in the corner of the tile.
     */
    RECTANGULAR_CORNER(3, ObjectGroup.WALL),

    /**
     * An object placed on a wall that can be interacted with by a player.
     */
    INTERACTABLE_WALL_DECORATION(4, ObjectGroup.WALL),

    /**
     * A wall that you can interact with.
     */
    INTERACTABLE_WALL(5, ObjectGroup.WALL),

    /**
     * A wall joint that is presented diagonally with respect to the tile.
     */
    DIAGONAL_WALL(9, ObjectGroup.WALL),

    /**
     * An object that can be interacted with by a player.
     */
    INTERACTABLE(10, ObjectGroup.INTERACTABLE_OBJECT),

    /**
     * An [INTERACTABLE] object, rotated `pi / 2` radians.
     */
    DIAGONAL_INTERACTABLE(11, ObjectGroup.INTERACTABLE_OBJECT),

    /**
     * A decoration positioned on the floor.
     */
    FLOOR_DECORATION(22, ObjectGroup.GROUND_DECORATION);

    companion object {
        val values = enumValues<ObjectType>()
    }
}

class GameObjectCollision(
    private val collisions: Collisions
) {

    fun applyUpdate(update: CollisionUpdate) {
        val type = update.type
        val map = update.flags

        for (entry in map.entries) {
            val tile = entry.key
            val pawns = MoreCollisionFlag.pawnFlags()
            val projectiles = MoreCollisionFlag.projectileFlags()

            for (flag in entry.value) {
                val direction = flag.direction
                if (direction == Direction.NONE) {
                    continue
                }

                val orientation = direction.orientationValue
                if (flag.impenetrable) {
                    flag(type, tile.x, tile.y, tile.plane, projectiles[orientation])
                }
                if(tile.equals(3208, 3214, 2)) {
                    println("Put flag. $orientation ${flag.impenetrable} ${pawns[orientation]}")
                }
                flag(type, tile.x, tile.y, tile.plane, pawns[orientation])
            }
        }
    }

    val Direction.orientationValue: Int
        get() = when(this) {
            Direction.NORTH_WEST -> 0
            Direction.NORTH -> 1
            Direction.NORTH_EAST -> 2
            Direction.EAST -> 4
            Direction.SOUTH_EAST -> 7
            Direction.SOUTH -> 6
            Direction.SOUTH_WEST -> 5
            Direction.WEST -> 3
            Direction.NONE -> -1
        }

    private fun flag(type: Int, x: Int, y: Int, plane: Int, flag: MoreCollisionFlag) {
        modifyMask(x, y, plane, flag.getBit(), type)
    }

    fun modifyCollision(gameObject: GameObject, changeType: Int) {
        modifyCollision(gameObject.def, gameObject.tile, gameObject.type, gameObject.rotation, changeType)
    }

    fun modifyCollision(def: ObjectDefinition, tile: Tile, type: Int, rotation: Int, changeType: Int) {
        val builder = Builder()
        builder.setType(changeType)
        builder.putObject(def, tile, type, rotation)
        applyUpdate(builder.build())
        /*if (def.solid == 0) {
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
        }*/
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
    private fun modifyWall(original: Tile, type: Int, rotation: Int, motion: Int, changeType: Int) {
        var tile = original

        // Internal corners
        if (type == 2) {
            // Mask both cardinal directions
            val or = when (Direction.ordinal[rotation and 0x3]) {
                Direction.NORTH_WEST -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_ROUTE_BLOCKER or org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST_ROUTE_BLOCKER //CollisionFlag.NORTH_OR_WEST
                Direction.NORTH_EAST -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_ROUTE_BLOCKER or org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST_ROUTE_BLOCKER
                Direction.SOUTH_EAST -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_ROUTE_BLOCKER or org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST_ROUTE_BLOCKER
                Direction.SOUTH_WEST -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_ROUTE_BLOCKER or org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST_ROUTE_BLOCKER
                else -> 0
            }
            modifyMask(original.x, original.y, original.plane, or, changeType)
            tile = tile.add(Direction.cardinal[(rotation + 3) and 0x3].delta)
        }

        // Mask one wall side
        var direction = when (type) {
            0 -> Direction.cardinal[(rotation + 3) and 0x3]
            2 -> Direction.cardinal[(rotation + 1) and 0x3]
            else -> Direction.ordinal[rotation and 0x3]
        }
        modifyMask(tile.x, tile.y, tile.plane, direction.flag(motion), changeType)

        // Mask other wall side
        tile = if (type == 2) {
            original.add(Direction.cardinal[rotation and 0x3].delta)
        } else {
            original.add(direction.delta)
        }
        direction = when (type) {
            2 -> Direction.cardinal[(rotation + 2) and 0x3]
            else -> direction.inverse()
        }
        modifyMask(tile.x, tile.y, tile.plane, direction.flag(motion), changeType)
    }

    private fun modifyMask(x: Int, y: Int, plane: Int, mask: Int, changeType: Any) {
        when (changeType) {
            ADD_MASK -> collisions.add(x, y, plane, mask)
            REMOVE_MASK -> collisions.remove(x, y, plane, mask)
            SET_MASK -> collisions[x, y, plane] = mask
        }
    }

    fun Direction.flag(motion: Int): Int {
        return when(this) {
            Direction.NORTH_WEST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_WEST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_WEST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_WEST
            }
            Direction.NORTH -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH
            }
            Direction.NORTH_EAST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_EAST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_EAST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_NORTH_EAST
            }
            Direction.EAST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_EAST
            }
            Direction.SOUTH_EAST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_EAST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_EAST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_EAST
            }
            Direction.SOUTH -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH
            }
            Direction.SOUTH_WEST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_WEST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_WEST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_SOUTH_WEST
            }
            Direction.WEST -> when(motion) {
                1 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST_PROJECTILE_BLOCKER
                2 -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST_ROUTE_BLOCKER
                else -> org.rsmod.pathfinder.flag.CollisionFlag.WALL_WEST
            }
            else -> 0
        }
    }

    companion object {
        const val ADD_MASK = 0
        const val REMOVE_MASK = 1
        const val SET_MASK = 2
    }
}