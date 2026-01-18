package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.attackers
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class DoorSupport : Script {

    init {
        npcOperate("Destroy", "door_support*") {
            message("This door does not seem to be openable from this side...")
        }

        npcDeath("door_support*") {
            for (attack in attackers) {
                attack.mode = EmptyMode
            }
            val base = GameObjects.find(tile, "door_support_base")
            base.remove(ticks = TimeUnit.SECONDS.toTicks(30))
        }
    }
}
