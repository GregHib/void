package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random
import java.util.LinkedList

object DungeonGenerator {
    data class DungeonDoor(
        val type: String,
    )

    data class DungeonRoom(
        var zone: Zone = Zone.EMPTY,
        var complexity: Int = 0,
        var type: String? = null,
        var doors: Array<DungeonDoor?> = arrayOfNulls(4),
        var children: Array<DungeonRoom?> = arrayOfNulls(4),
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val width = 4
        val height = 4
        val dungeon = arrayOfNulls<DungeonRoom>(width * height)
        val complexity = 1
        var roomCount = (50).coerceAtMost(width * height)
        val keys = (0..5).toMutableList()

        val bossTile = Tile(random.nextInt(width), random.nextInt(height))
        val dirs = validDirection(bossTile)
        val dir = Direction.cardinal[dirs.indexOfFirst { it != Search.Blocked }]
//        val bossRoom = validRooms(dir, complexity).random()
        val bossRoom = DungeonRoom(complexity = complexity, type = "boss")
        dungeon[index(bossTile, width, height)] = bossRoom

        /*
            stack = Stack<Room, Door>
            doorCount = 5
            while(stack) {
              room, door = pop()
              nextRoom = dungeon + door
              req = getRoomReq(nextRoom)
              newRoom = pickRoom(req, complexity, doorCount)
              pickDoor(newRoom, door.inverse(), boss = room.boss)
              for(d in newRoom.doors) {
              if d == door continue
                stack add(newRoom, d)
              }
            }
         */

        val tile = bossTile.add(dir.delta)
        val index = dir.inverse().index
        val doors = Array(4) { Search.Optional }
        doors[index] = Search.Required
        val nextRoom = room(complexity, doors)
        roomCount--
        nextRoom.children[index] = bossRoom
        if (keys.isNotEmpty()) {
            val key = keys.removeFirst()
            nextRoom.doors[index] = DungeonDoor("key")
        }
        val stack = LinkedList<Pair<DungeonRoom, Int>>()
        while (stack.isNotEmpty()) {
            val (room, index) = stack.pop()
            if (roomCount-- < 0) {
                // Or switch from critical path
                break
            }

            // Add door
            // Queue room
        }
    }

    val Direction.index: Int
        get() = ordinal / 2

    fun index(tile: Tile, width: Int, height: Int) = 0

    enum class Search {
        Required,
        Optional,
        Blocked
    }

    fun room(complexity: Int, doors: Array<Search>): DungeonRoom {
        // TODO update doors as well
        return DungeonRoom(type = "random")
    }

    fun validRooms(direction: Direction, complexity: Int): List<Int> {
        return listOf()
    }

    fun validDirection(tile: Tile): Array<Search> {
        return Array(4) { Search.Optional }
    }

}
