package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class DungeonWorldMap : Script {
    init {
        interfaceOpened("dungeon_map") {
            val dungeon = dungeonMap ?: return@interfaceOpened
            sendScript("dung_map_reset")
            val tile = dungeon.start
            sendScript("dung_map_start_room", dungeon.start.x, dungeon.start.y, 1)
            sendScript("dung_map_add_player", dungeon.start.x, dungeon.start.y, index, 1, name)
            val startRoom = dungeon.room(dungeon.start.x, dungeon.start.y)!!
            addRoom(startRoom, Direction.NONE, tile)
            for ((index, door) in startRoom.doors.withIndex()) {
                if (door == null) {
                    continue
                }
                val dir = Direction.westClockwise[index]
                val tile = tile.add(dir)
                val room = dungeon.room(tile.x, tile.y) ?: continue
                addRoom(room, dir, tile)
            }
        }
    }

    private fun Player.addRoom(room: DungeonRoom, dir: Direction, tile: Tile) {
        when (room.type) {
            DungeonRoomType.Base -> sendScript("dung_map_start_room", room.tile.x, room.tile.y, 1)
            DungeonRoomType.Boss -> sendScript("dung_map_boss_room", room.tile.x, room.tile.y, 1)
            else -> {}
        }
        var id = if (room.open) {
            val directions = room.doors.mapIndexed { index, door -> if (door != null) Direction.westClockwise[index] else null }.filterNotNull().toSet()
            when (directions) {
                setOf(Direction.SOUTH) -> 2791
                setOf(Direction.WEST) -> 2792
                setOf(Direction.NORTH) -> 2793
                setOf(Direction.EAST) -> 2794
                setOf(Direction.SOUTH, Direction.WEST) -> 2795
                setOf(Direction.NORTH, Direction.WEST) -> 2796
                setOf(Direction.NORTH, Direction.EAST) -> 2797
                setOf(Direction.SOUTH, Direction.EAST) -> 2798
                setOf(Direction.SOUTH, Direction.WEST, Direction.NORTH) -> 2799
                setOf(Direction.WEST, Direction.EAST, Direction.NORTH) -> 2800
                setOf(Direction.SOUTH, Direction.EAST, Direction.NORTH) -> 2801
                setOf(Direction.SOUTH, Direction.EAST, Direction.WEST) -> 2802
                setOf(Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH) -> 2803
                setOf(Direction.WEST, Direction.EAST) -> 2804
                else -> return
            }
        } else {
            when (dir.inverse()) {
                Direction.SOUTH -> 2787
                Direction.WEST -> 2788
                Direction.NORTH -> 2789
                Direction.EAST -> 2790
                else -> return
            }
        }
        if (get("guide_mode", false) && room.isCritical) {
            id += 19
        }
        sendScript("dung_map_add_room", tile.x, tile.y, id, 1)
    }
}