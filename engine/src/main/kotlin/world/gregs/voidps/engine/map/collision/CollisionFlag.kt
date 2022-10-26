package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.engine.entity.Direction

object CollisionFlag {

    const val WATER = 0x200000
    const val FLOOR = WATER.inv()
    const val FLOOR_DECO = 0x40000
    const val NPC = 0x80000
    const val PLAYER = 0x100000
    const val ENTITY = PLAYER or NPC

    const val LAND = 0x100
    const val SKY = LAND shl 9
    const val IGNORED = LAND shl 22

    const val BLOCKED = LAND or WATER or FLOOR_DECO

    const val NORTH_WEST = 0x1
    const val NORTH = 0x2
    const val NORTH_EAST = 0x4
    const val EAST = 0x8
    const val SOUTH_EAST = 0x10
    const val SOUTH = 0x20
    const val SOUTH_WEST = 0x40
    const val WEST = 0x80

    const val NORTH_OR_WEST = NORTH or WEST
    const val NORTH_OR_EAST = NORTH or EAST
    const val SOUTH_OR_EAST = SOUTH or EAST
    const val SOUTH_OR_WEST = SOUTH or WEST

    const val NORTH_AND_WEST = NORTH_OR_WEST or NORTH_WEST
    const val NORTH_AND_EAST = NORTH_OR_EAST or NORTH_EAST
    const val SOUTH_AND_EAST = SOUTH_OR_EAST or SOUTH_EAST
    const val SOUTH_AND_WEST = SOUTH_OR_WEST or SOUTH_WEST

    const val NOT_NORTH = SOUTH or EAST or WEST or SOUTH_EAST or SOUTH_WEST
    const val NOT_EAST = NORTH or SOUTH or WEST or NORTH_WEST or SOUTH_WEST
    const val NOT_SOUTH = NORTH or EAST or WEST or NORTH_EAST or NORTH_WEST
    const val NOT_WEST = NORTH or SOUTH or EAST or NORTH_EAST or SOUTH_EAST

    fun rotate(flag: Int, rotation: Int): Int {
        val original = flag
        var flag = flag
        flag = transform(original, flag, rotation * 2) { it.flag() shl 22 }
        flag = transform(original, flag, rotation * 2) { it.flag() shl 9 }
        flag = transform(original, flag, rotation * 2) { it.flag() }
        return flag
    }

    private fun transform(original: Int, flag: Int, rotation: Int, toFlag: (Direction) -> Int): Int {
        var flag = flag
        for (dir in Direction.all) {
            if (check(original, toFlag(dir))) {
                flag = remove(flag, toFlag(dir))
                flag = add(flag, toFlag(dir.rotate(rotation)))
            }
        }
        return flag
    }


    private fun check(value: Int, flag: Int): Boolean = value and flag != 0

    private fun remove(value: Int, flag: Int): Int = value and flag.inv()

    private fun add(value: Int, flag: Int): Int = value or flag

    @JvmStatic
    fun main(args: Array<String>) {
        findOverlap(65664)
    }

    private fun findOverlap(flag: Int) {
        val result = StringBuilder()

        result.append(this.javaClass.name)
        result.append(" Object {")
        result.appendLine()

        //determine fields declared in this class only (no fields of superclass)
        val fields = this.javaClass.declaredFields

        //print field names paired with their values
        for (field in fields) {
            try {
                val value = field.get(this)
                if (value is Int && flag and value != 0) {
                    result.append("  ")
                    result.append(field.name)
                    result.append(": ")
                    result.append("$value 0x${"%X".format(value)} ${Integer.toBinaryString(value)}")
                    result.appendLine()
                }
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }

        }
        result.append("}")

        println(result.toString())
    }
}

fun Direction.flag() = when (this) {
    Direction.NORTH_WEST -> CollisionFlag.NORTH_WEST
    Direction.NORTH -> CollisionFlag.NORTH
    Direction.NORTH_EAST -> CollisionFlag.NORTH_EAST
    Direction.EAST -> CollisionFlag.EAST
    Direction.SOUTH_EAST -> CollisionFlag.SOUTH_EAST
    Direction.SOUTH -> CollisionFlag.SOUTH
    Direction.SOUTH_WEST -> CollisionFlag.SOUTH_WEST
    Direction.WEST -> CollisionFlag.WEST
    Direction.NONE -> 0
}

fun Direction.and() = when (this) {
    Direction.NORTH_WEST -> CollisionFlag.NORTH_AND_WEST
    Direction.NORTH -> CollisionFlag.NORTH
    Direction.NORTH_EAST -> CollisionFlag.NORTH_AND_EAST
    Direction.EAST -> CollisionFlag.EAST
    Direction.SOUTH_EAST -> CollisionFlag.SOUTH_AND_EAST
    Direction.SOUTH -> CollisionFlag.SOUTH
    Direction.SOUTH_WEST -> CollisionFlag.SOUTH_AND_WEST
    Direction.WEST -> CollisionFlag.WEST
    Direction.NONE -> 0
}

fun Direction.not() = when (this) {
    Direction.NORTH_WEST -> CollisionFlag.SOUTH_AND_EAST
    Direction.NORTH -> CollisionFlag.NOT_NORTH
    Direction.NORTH_EAST -> CollisionFlag.SOUTH_AND_WEST
    Direction.EAST -> CollisionFlag.NOT_EAST
    Direction.SOUTH_EAST -> CollisionFlag.NORTH_AND_WEST
    Direction.SOUTH -> CollisionFlag.NOT_SOUTH
    Direction.SOUTH_WEST -> CollisionFlag.NORTH_AND_EAST
    Direction.WEST -> CollisionFlag.NOT_WEST
    Direction.NONE -> 0
}