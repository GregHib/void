package content.area.misthalin.edgeville.stronghold_of_player_safety

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class CockroachSoldier : Script {

    init {
        npcCondition("ranged_only") { target ->
            target is Player && tile.distanceTo(target.tile) > 2
        }
    }
}
