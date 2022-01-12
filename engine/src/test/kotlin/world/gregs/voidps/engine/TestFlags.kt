package world.gregs.voidps.engine

import world.gregs.voidps.engine.map.collision.CollisionFlag.BLOCKED
import world.gregs.voidps.engine.map.collision.CollisionFlag.EAST
import world.gregs.voidps.engine.map.collision.CollisionFlag.IGNORED
import world.gregs.voidps.engine.map.collision.CollisionFlag.NORTH
import world.gregs.voidps.engine.map.collision.CollisionFlag.NORTH_AND_EAST
import world.gregs.voidps.engine.map.collision.CollisionFlag.NORTH_AND_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.NOT_EAST
import world.gregs.voidps.engine.map.collision.CollisionFlag.NOT_NORTH
import world.gregs.voidps.engine.map.collision.CollisionFlag.NOT_SOUTH
import world.gregs.voidps.engine.map.collision.CollisionFlag.NOT_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.SKY
import world.gregs.voidps.engine.map.collision.CollisionFlag.SOUTH
import world.gregs.voidps.engine.map.collision.CollisionFlag.SOUTH_AND_EAST
import world.gregs.voidps.engine.map.collision.CollisionFlag.SOUTH_AND_WEST
import world.gregs.voidps.engine.map.collision.CollisionFlag.WEST

object TestFlags {
    const val LAND_BLOCK_NORTH_WEST = NORTH_AND_WEST or BLOCKED
    const val LAND_BLOCK_NORTH = NORTH or BLOCKED
    const val LAND_BLOCK_NORTH_EAST = NORTH_AND_EAST or BLOCKED
    const val LAND_BLOCK_EAST = EAST or BLOCKED
    const val LAND_BLOCK_SOUTH_EAST = SOUTH_AND_EAST or BLOCKED
    const val LAND_BLOCK_SOUTH = SOUTH or BLOCKED
    const val LAND_BLOCK_SOUTH_WEST = SOUTH_AND_WEST or BLOCKED
    const val LAND_BLOCK_WEST = WEST or BLOCKED

    const val LAND_WALL_NORTH_WEST = NORTH_AND_WEST or BLOCKED
    const val LAND_WALL_NORTH = NORTH or BLOCKED
    const val LAND_WALL_NORTH_EAST = NORTH_AND_EAST or BLOCKED
    const val LAND_WALL_EAST = EAST or BLOCKED
    const val LAND_WALL_SOUTH_EAST = SOUTH_AND_EAST or BLOCKED
    const val LAND_WALL_SOUTH = SOUTH or BLOCKED
    const val LAND_WALL_SOUTH_WEST = SOUTH_AND_WEST or BLOCKED
    const val LAND_WALL_WEST = WEST or BLOCKED

    const val LAND_CLEAR_NORTH_WEST = SOUTH_AND_EAST or BLOCKED
    const val LAND_CLEAR_NORTH = NOT_NORTH or BLOCKED
    const val LAND_CLEAR_NORTH_EAST = SOUTH_AND_WEST or BLOCKED
    const val LAND_CLEAR_EAST = NOT_EAST or BLOCKED
    const val LAND_CLEAR_SOUTH_EAST = NORTH_AND_WEST or BLOCKED
    const val LAND_CLEAR_SOUTH = NOT_SOUTH or BLOCKED
    const val LAND_CLEAR_SOUTH_WEST = NORTH_AND_EAST or BLOCKED
    const val LAND_CLEAR_WEST = NOT_WEST or BLOCKED


    const val SKY_BLOCK_NORTH_WEST = NORTH_AND_WEST shl 9 or SKY
    const val SKY_BLOCK_NORTH = NORTH shl 9 or SKY
    const val SKY_BLOCK_NORTH_EAST = NORTH_AND_EAST shl 9 or SKY
    const val SKY_BLOCK_EAST = EAST shl 9 or SKY
    const val SKY_BLOCK_SOUTH_EAST = SOUTH_AND_EAST shl 9 or SKY
    const val SKY_BLOCK_SOUTH = SOUTH shl 9 or SKY
    const val SKY_BLOCK_SOUTH_WEST = SOUTH_AND_WEST shl 9 or SKY
    const val SKY_BLOCK_WEST = WEST shl 9 or SKY

    const val SKY_WALL_NORTH_WEST = NORTH_AND_WEST shl 9 or SKY
    const val SKY_WALL_NORTH = NORTH shl 9 or SKY
    const val SKY_WALL_NORTH_EAST = NORTH_AND_EAST shl 9 or SKY
    const val SKY_WALL_EAST = EAST shl 9 or SKY
    const val SKY_WALL_SOUTH_EAST = SOUTH_AND_EAST shl 9 or SKY
    const val SKY_WALL_SOUTH = SOUTH shl 9 or SKY
    const val SKY_WALL_SOUTH_WEST = SOUTH_AND_WEST shl 9 or SKY
    const val SKY_WALL_WEST = WEST shl 9 or SKY

    const val SKY_CLEAR_NORTH_WEST = SOUTH_AND_EAST shl 9 or SKY
    const val SKY_CLEAR_NORTH = NOT_NORTH shl 9 or SKY
    const val SKY_CLEAR_NORTH_EAST = SOUTH_AND_WEST shl 9 or SKY
    const val SKY_CLEAR_EAST = NOT_EAST shl 9 or SKY
    const val SKY_CLEAR_SOUTH_EAST = NORTH_AND_WEST shl 9 or SKY
    const val SKY_CLEAR_SOUTH = NOT_SOUTH shl 9 or SKY
    const val SKY_CLEAR_SOUTH_WEST = NORTH_AND_EAST shl 9 or SKY
    const val SKY_CLEAR_WEST = NOT_WEST shl 9 or SKY

    const val IGNORED_BLOCK_NORTH_WEST = NORTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_BLOCK_NORTH = NORTH shl 22 or IGNORED
    const val IGNORED_BLOCK_NORTH_EAST = NORTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_BLOCK_EAST = EAST shl 22 or IGNORED
    const val IGNORED_BLOCK_SOUTH_EAST = SOUTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_BLOCK_SOUTH = SOUTH shl 22 or IGNORED
    const val IGNORED_BLOCK_SOUTH_WEST = SOUTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_BLOCK_WEST = WEST shl 22 or IGNORED

    const val IGNORED_WALL_NORTH_WEST = NORTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_WALL_NORTH = NORTH shl 22 or IGNORED
    const val IGNORED_WALL_NORTH_EAST = NORTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_WALL_EAST = EAST shl 22 or IGNORED
    const val IGNORED_WALL_SOUTH_EAST = SOUTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_WALL_SOUTH = SOUTH shl 22 or IGNORED
    const val IGNORED_WALL_SOUTH_WEST = SOUTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_WALL_WEST = WEST shl 22 or IGNORED

    const val IGNORED_CLEAR_NORTH_WEST = SOUTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_CLEAR_NORTH = NOT_NORTH shl 22 or IGNORED
    const val IGNORED_CLEAR_NORTH_EAST = SOUTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_CLEAR_EAST = NOT_EAST shl 22 or IGNORED
    const val IGNORED_CLEAR_SOUTH_EAST = NORTH_AND_WEST shl 22 or IGNORED
    const val IGNORED_CLEAR_SOUTH = NOT_SOUTH shl 22 or IGNORED
    const val IGNORED_CLEAR_SOUTH_WEST = NORTH_AND_EAST shl 22 or IGNORED
    const val IGNORED_CLEAR_WEST = NOT_WEST shl 22 or IGNORED


    @JvmStatic
    fun main(args: Array<String>) {
        printAll()
    }

    private fun findOverlap(flag: Int) {
        val result = StringBuilder()
        val newLine = System.getProperty("line.separator")

        result.append(this.javaClass.name)
        result.append(" Object {")
        result.append(newLine)

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
                    result.append(newLine)
                }
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }

        }
        result.append("}")

        println(result.toString())
    }

    private fun printAll() {
        val result = StringBuilder()
        val newLine = System.getProperty("line.separator")

        result.append(this.javaClass.name)
        result.append(" Object {")
        result.append(newLine)

        //determine fields declared in this class only (no fields of superclass)
        val fields = this.javaClass.declaredFields

        //print field names paired with their values
        for (field in fields) {
            result.append("  ")
            try {
                result.append(field.name)
                result.append(": ")
                //requires access to private field:
                val value = field.get(this)
                if (value is Int) {
                    result.append("$value 0x${"%X".format(value)} ${Integer.toBinaryString(value)}")
                } else {
                    result.append(field.get(this))
                }
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }

            result.append(newLine)
        }
        result.append("}")

        println(result.toString())
    }
}