package content.skill.runecrafting

import content.entity.proj.shoot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue

object EssenceMine {
    fun teleport(npc: NPC, player: Player) {
        npc.say("Senventior Disthine Molenko!")
        npc.gfx("curse_cast")
        npc.face(player)
        npc.anim("curse")
        player.gfx("curse_impact")
        player.shoot("curse", player.tile)
        player.softQueue("essence_mine_teleport", 3) {
            player["last_npc_teleport_to_rune_essence_mine"] = npc.id
            val tile = AreaTypes["essence_mine_teleport"].random(player)!!
            player.tele(tile)
            if (player["enter_the_abyss", "unstarted"] == "scrying") {
                player["scrying_orb_${npc.id}"] = true
                val count = player.variables.data.count { it.key.startsWith("scrying_orb_") }
                if (count == 1) {
                    player.message("Your scrying orb has absorbed enough teleportation information.")
                    player.inventory.replace("scrying_orb", "scrying_orb_full")
                }
            }
        }
    }
}
