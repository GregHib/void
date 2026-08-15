package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import world.gregs.voidps.engine.Script

class DungeonDeaths : Script {
    init {
        playerDeath { onDeath ->
            if (inDungeoneering) {
                val map = dungeonMap!!
                val room = map.start()
                val tile = map.tile(room)
                onDeath.teleport = tile.add(8, 6)
                inc("dungeon_deaths")
            }
        }
    }
}
