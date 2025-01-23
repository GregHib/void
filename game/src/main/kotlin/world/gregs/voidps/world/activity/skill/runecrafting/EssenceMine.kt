package world.gregs.voidps.world.activity.skill.runecrafting

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.interact.entity.proj.shoot

object EssenceMine {
    fun teleport(npc: NPC, player: Player) {
        npc.say("Senventior Disthine Molenko!")
        npc.gfx("curse_cast")
        npc.face(player)
        if (!npc.contains("old_model")) {
            npc.setAnimation("curse")
        }
        player.gfx("curse_hit")
        player.shoot("curse", player.tile)
        player.softQueue("essence_mine_teleport", 3) {
            player["last_npc_teleport_to_rune_essence_mine"] = npc.id
            val areas: AreaDefinitions = get()
            val tile = areas["essence_mine_teleport"].random(player)!!
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