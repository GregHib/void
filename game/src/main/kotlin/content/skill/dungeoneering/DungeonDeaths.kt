package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.name

class DungeonDeaths : Script {
    init {
        playerDeath { onDeath ->
            if (inDungeoneering) {
                val map = dungeonMap!!
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
        }
    }
}
