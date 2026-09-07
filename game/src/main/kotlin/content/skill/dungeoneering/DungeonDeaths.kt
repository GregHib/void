package content.skill.dungeoneering

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.combat.killer
import content.entity.world.music.playTrack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

class DungeonDeaths : Script {
    init {
        playerDeath { onDeath ->
            if (!inDungeoneering) {
                return@playerDeath
            }
            val map = dungeonMap ?: return@playerDeath
            val room = map.start()
            val tile = map.tile(room)
            onDeath.teleport = tile.add(8, 6)
            inc("dungeon_deaths")
            for (member in dungeonMembers) {
                if (member != this) {
                    // https://youtu.be/ouT__1cWTTU?t=557
                    message("$name was killed.")
                }
            }
        }

        npcDeath {
            val player = killer as? Player ?: return@npcDeath
            if (!player.inDungeoneering) {
                return@npcDeath
            }
            val map = player.dungeonMap ?: return@npcDeath
            val room = map.room(tile) ?: return@npcDeath
            room.monsters--
        }

        npcDeath("rand_ice_lord_boss_*") {
            val room = dungeonRoomBounds()
            var door = findDoor(room.minX - 1, room.minY - 1, "rand_dungeon_end_trapdoor_locked_frozen")
            if (door == null) {
                for (tile in Rectangle(room.minX - 1, room.minY - 1, room.minX + 1, room.minY + 1)) {
                    val obj = GameObjects.findOrNull(tile, "rand_dungeon_end_trapdoor_locked_frozen")
                    if (obj != null) {
                        door = obj
                    }
                }
                logger.warn { "Error finding dungeon door: $door" }
            }
            door?.replace("rand_dungeon_end_trapdoor_unlocked_frozen")
            // TODO rewards (based on combat?)
            // https://youtu.be/nSob5r5-UtE?t=563
            // You received item:

            // https://youtu.be/2aX5poT8Fnk?t=496
            // <username> received item:
            val killer = killer as? Player ?: return@npcDeath
            val map = killer.dungeonMap ?: return@npcDeath
            killer.playTrack(DungeonMusic.combatTrack(map.theme))
        }
    }

    private val logger = InlineLogger()

    private fun findDoor(x: Int, y: Int, id: String): GameObject? = GameObjects.findOrNull(Tile(x, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 15, y + 7), id) ?: GameObjects.findOrNull(Tile(x + 7, y), id) ?: GameObjects.findOrNull(Tile(x + 7, y + 15), id)
}
