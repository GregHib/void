package content.skill.runecrafting

import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.queue.softQueue

class RuneEssenceMine : Script {

    init {
        objectOperate("Enter", "rune_essence_exit_portal") { (target) ->
            message("You step through the portal...")
            gfx("curse_impact", delay = 30)
            target.tile.shoot("curse", tile)

            softQueue("essence_mine_exit", 3) {
                val npc = get("last_npc_teleport_to_rune_essence_mine", "aubury")
                val tile = AreaDefinitions["${npc}_return"].random()
                tele(tile)
            }
        }
    }
}
