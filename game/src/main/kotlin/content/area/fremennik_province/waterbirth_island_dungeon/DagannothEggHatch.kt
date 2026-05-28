package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class DagannothEggHatch : Script {

    init {
        /**
         * When a player enters hunt radius, egg transforms to spawn and attacks.
         */
        huntPlayer("dagannoth_egg", "aggressive") { target ->
            // Ignore if already transformed
            if (transform == "dagannoth_spawn") {
                return@huntPlayer
            }
            transform("dagannoth_egg_open")

            // Small stand-up delay before attacking
            queue("dagannoth_hatch_attack", 1) {
                transform("dagannoth_egg_opened")
                NPCs.add("dagannoth_spawn", tile)
                interactPlayer(target, "Attack")
            }
            queue("dagannoth_respawn", TimeUnit.MINUTES.toTicks(5)) {
                transform("dagannoth_egg")
            }
        }
    }
}
