package content.minigame.sorceress_garden

import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.strongQueue

class Elementals(val patrols: PatrolDefinitions, val players: Players) : Script {

    init {
        npcSpawn("autumn_elemental*,spring_elemental*,summer_elemental*,winter_elemental*") {
            val patrol = patrols.get(id)
            mode = Patrol(this, patrol.waypoints)
        }

        huntPlayer("*_elemental*", "spotted") {
            val direction = direction
            // Catch all players two tiles in-front
            for (player in players.at(tile.add(direction))) {
                catch(player)
            }
            for (player in players.at(tile.add(direction).add(direction))) {
                catch(player)
            }
        }
    }

    fun catch(player: Player) {
        if (player.queue.contains("sorceress_garden_caught")) {
            return
        }
        player.strongQueue("sorceress_garden_caught") {
            player.anim("elemental_pointing")
            player.sound("stun_all")
            player.shoot("curse", player.tile)
            player.gfx("curse_impact")
            player.open("fade_out")
            player.delay(4)
            player.tele(2911, 5470)
            player.open("fade_in")
            player.sound("stunned")
        }
    }
}
