package content.skill.dungeoneering.boss

import content.skill.dungeoneering.dungeonRoom
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.random.nextInt

class IcyBones : Script {
    init {
        npcSpawn("rand_ice_lord_boss_*") {
            anim("icy_bones_spawn")
        }

        npcAttack("icy_bones", "ice_box") {
            // TODO what if under?
            iceBox()
        }

        npcAttack("icy_bones", "ice_cone") {
            iceCone()
        }
    }

    private fun Character.iceCone() {
        val room = dungeonRoom()
        val dir = direction
        val left = when (dir) {
            Direction.NORTH -> tile.add(-1, size - 1)
            Direction.NORTH_EAST -> tile.add(size - 1, size)
            Direction.EAST -> tile.add(size - 1, size)
            Direction.SOUTH_EAST -> tile.add(size, 0)
            Direction.SOUTH -> tile.add(size, 0)
            Direction.SOUTH_WEST -> tile.add(0, -1)
            Direction.WEST -> tile.add(0, -1)
            Direction.NORTH_WEST -> tile.add(-1, size - 1)
            Direction.NONE -> return
        }
        val right = when (dir) {
            Direction.NORTH -> tile.add(size, size - 1)
            Direction.NORTH_EAST -> tile.add(size, size - 1)
            Direction.EAST -> tile.add(size - 1, -1)
            Direction.SOUTH_EAST -> tile.add(size - 1, -1)
            Direction.SOUTH -> tile.add(-1, 0)
            Direction.SOUTH_WEST -> tile.add(-1, 0)
            Direction.WEST -> tile.add(0, size)
            Direction.NORTH_WEST -> tile.add(0, size)
            Direction.NONE -> return
        }
        stalagmite(left)
        stalagmite(right)
        side(left, dir, room, 6)
        side(right, dir, room, 2)
    }

    private fun side(left: Tile, dir: Direction, room: Rectangle, rotation: Int) {
        var tile = left
        for (i in 0 until 3) {
            tile = tile.add(dir).add(dir.rotate(rotation))
            if (!room.contains(tile)) {
                break
            }
            if (random.nextInt(5) != 0) {
                stalagmite(tile)
            }
            for (j in 0 until 2) {
                tile = tile.add(dir)
                if (!room.contains(tile)) {
                    break
                }
                if (random.nextInt(5) != 0) {
                    stalagmite(tile)
                }
            }
        }
    }

    private fun Character.iceBox() {
        val room = dungeonRoom()
        for (x in tile.x - 2..tile.x + 2) {
            val top = Tile(x, tile.y + 3)
            if (room.contains(top)) {
                stalagmite(top)
            }
            val bottom = Tile(x, tile.y - 3)
            if (room.contains(bottom)) {
                stalagmite(bottom)
            }
        }
        for (y in tile.y - 2..tile.y + 2) {
            val right = Tile(tile.x + 3, y)
            if (room.contains(right)) {
                stalagmite(right)
            }
            val left = Tile(tile.x - 3, y)
            if (room.contains(left)) {
                stalagmite(left)
            }
        }
    }

    private fun stalagmite(left: Tile) {
        GameObjects.add("rand_stalagmite_clone_${random.nextInt(1..3)}", left, ticks = TimeUnit.SECONDS.toTicks(random.nextInt(6..8)))
    }
}
