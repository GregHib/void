package world.gregs.voidps.world.activity.skill.runecrafting

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.interact.entity.proj.shoot

object EssenceMine {
    /*
        TODO randomise target teleport
        # aubury
        2922, y = 4845
        # sedridor
        2922, y = 4810

        #brimstail
        2921, y = 4856
        2913, y = 4838
        2935, y = 4820
        2894, y = 4838

        #distentor
        2915, y = 4832
        2912, y = 4838

        # cromperty
        2896, y = 4809
        2932, y = 4842

     */
    fun teleport(npc: NPC, player: Player) {
        npc.forceChat = "Senventior Disthine Molenko!"
        npc.setGraphic("curse_cast")
        player.setGraphic("curse_hit")
        player.shoot("curse", player.tile, offset = 64)
        player.softQueue("essence_mine_teleport", 3) {
            player["last_npc_teleport_to_rune_essence_mine"] = npc.id
            player.tele(2935, 4820)
            if (player["into_the_abyss", "unstarted"] == "stage1") {
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