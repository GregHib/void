package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.effect.transform
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class DagannothEggHatch {

    val npcs: NPCs by inject()

    init {
        /**
         * When a player enters hunt radius, egg transforms to spawn and attacks.
         */
        huntPlayer("dagannoth_egg", "aggressive") { npc ->
            // Ignore if already transformed
            if (npc.transform == "dagannoth_spawn") {
                return@huntPlayer
            }
            npc.transform("dagannoth_egg_open")

            // Small stand-up delay before attacking
            npc.softQueue("dagannoth_hatch_attack", 1) {
                npc.transform("dagannoth_egg_opened")
                val spawn = npcs.add("dagannoth_spawn", npc.tile)
                spawn.mode = Interact(spawn, target, PlayerOption(spawn, target, "Attack"))
            }
            npc.softQueue("dagannoth_respawn", TimeUnit.MINUTES.toTicks(5)) {
                npc.transform("dagannoth_egg")
            }
        }
    }
}
