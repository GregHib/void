package content.skill.runecrafting

import content.entity.proj.shoot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue

@Script
class RuneEssenceMine {

    val areas: AreaDefinitions by inject()

    init {
        objectOperate("Enter", "rune_essence_exit_portal") {
            player.message("You step through the portal...")
            player.gfx("curse_impact", delay = 30)
            target.tile.shoot("curse", player.tile)

            player.softQueue("essence_mine_exit", 3) {
                val npc = player["last_npc_teleport_to_rune_essence_mine", "aubury"]
                val tile = areas["${npc}_return"].random()
                player.tele(tile)
            }
        }
    }
}
