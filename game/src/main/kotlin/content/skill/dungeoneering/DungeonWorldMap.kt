package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction

class DungeonWorldMap : Script {
    init {
        interfaceOpened("dungeon_map") {
            val dungeon = dungeonMap ?: return@interfaceOpened
            sendScript("dung_map_reset")
            for (room in dungeon.traverse { from, _, _ -> from.open }) {
                showRoom(room, room.tile.delta(room.parent?.tile ?: room.tile).toDirection())
            }
            for (index in dungeon.players) {
                val player = Players.indexed(index) ?: continue
                val delta = player.tile.delta(dungeon.region.tile)
                val room = Delta(delta.x / 16, delta.y / 16)
                sendScript("dung_map_add_player", room.x, room.y, index, 1, player.name)
            }
        }
    }

    private fun Player.showRoom(room: DungeonRoom, dir: Direction = Direction.NONE) {
        when (room.type) {
            DungeonRoomType.Base -> sendScript("dung_map_start_room", room.tile.x, room.tile.y, 1)
            DungeonRoomType.Boss -> sendScript("dung_map_boss_room", room.tile.x, room.tile.y, 1)
            else -> {}
        }
        var id = if (room.open) {
            when (room.doors.foldIndexed(0) { index, acc, door -> if (door != null) acc or (1 shl index) else acc }) {
                SOUTH -> 2791
                WEST -> 2792
                NORTH -> 2793
                EAST -> 2794
                SOUTH or WEST -> 2795
                NORTH or WEST -> 2796
                NORTH or EAST -> 2797
                SOUTH or EAST -> 2798
                SOUTH or WEST or NORTH -> 2799
                WEST or EAST or NORTH -> 2800
                SOUTH or EAST or NORTH -> 2801
                SOUTH or EAST or WEST -> 2802
                SOUTH or EAST or WEST or NORTH -> 2803
                WEST or EAST -> 2804
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
        sendScript("dung_map_add_room", room.tile.x, room.tile.y, id, 1)
    }

    companion object {
        private const val WEST = 1 shl 0
        private const val NORTH = 1 shl 1
        private const val EAST = 1 shl 2
        private const val SOUTH = 1 shl 3
    }
}
