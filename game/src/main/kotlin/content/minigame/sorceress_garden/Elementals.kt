package content.minigame.sorceress_garden

import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.HuntModeDefinitions
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import kotlin.getValue

class Elementals : Script {
    val patrols: PatrolDefinitions by inject()
    val hunting: Hunting by inject()
    val players: Players by inject()
    val huntModes: HuntModeDefinitions by inject()

    init {
        npcSpawn("autumn_elemental*,spring_elemental*,summer_elemental*,winter_elemental*") {
            val patrol = patrols.get(id)
            mode = Patrol(this, patrol.waypoints)
        }

        huntPlayer("*_elemental*", "spotted") {
            val direction = direction
            val modeDefinition = huntModes.get("spotted")
            val targets = hunting.getCharacters(this, players, def["hunt_range", 5], modeDefinition)
            for (player in targets) {
                if (direction != Direction.NONE && direction != player.tile.delta(tile).toDirection()) {
                    continue // Skip players that aren't in-front or under.
                }
                if (player.queue.contains("sorceress_garden_caught")) {
                    continue
                }
                player.strongQueue("sorceress_garden_caught") {
                    anim("elemental_pointing")
                    player.sound("stun_all")
                    shoot("curse", player.tile)
                    player.gfx("curse_impact")
                    player.open("fade_out")
                    delay(4)
                    player.tele(2911, 5470)
                    player.open("fade_in")
                    player.sound("stunned")
                }
            }
        }
    }
}
