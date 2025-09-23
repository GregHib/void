package content.minigame.sorceress_garden

import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import kotlin.getValue

@Script
class Elementals : Api {
    val patrols: PatrolDefinitions by inject()

    override fun spawn(npc: NPC) {
        if (npc.id.startsWith("autumn_elemental") || npc.id.startsWith("spring_elemental") || npc.id.startsWith("summer_elemental") || npc.id.startsWith("winter_elemental")) {
            val patrol = patrols.get(npc.id)
            npc.mode = Patrol(npc, patrol.waypoints)
        }
    }

    init {
        huntPlayer("*_elemental*", "spotted") { npc ->
            val direction = npc.direction
            for (player in targets) {
                if (direction != Direction.NONE && direction != player.tile.delta(npc.tile).toDirection()) {
                    continue // Skip players that aren't in-front or under.
                }
                if (player.queue.contains("sorceress_garden_caught")) {
                    continue
                }
                player.strongQueue("sorceress_garden_caught") {
                    npc.anim("elemental_pointing")
                    player.sound("stun_all")
                    npc.shoot("curse", player.tile)
                    player.gfx("curse_hit")
                    player.open("fade_out")
                    delay(2)
                    player.tele(2911, 5470)
                    player.open("fade_in")
                    player.sound("stunned")
                }
            }
        }
    }
}