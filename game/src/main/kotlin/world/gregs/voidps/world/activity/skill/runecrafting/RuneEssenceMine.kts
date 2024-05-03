package world.gregs.voidps.world.activity.skill.runecrafting

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.interact.entity.proj.shoot

// TODO replace with areas - ${npc.id}_return_teleport
val teleports = mapOf(
    "aubury" to Tile(3252, 3401),
    "brimstail" to Tile(2411, 9814), // 2410, 9814
    "wizard_cromperty" to Tile(2684, 3321), // 2684, 3323
    "wizard_distentor" to Tile(2592, 3084), // 2593, 3085
    "sedridor" to Tile(3106, 9573),
)

objectOperate("Enter", "rune_essence_exit_portal") {
    player.message("You step through the portal...")
    player.setGraphic("curse_hit", delay = 30)
    target.tile.shoot("curse", offset = 64)

    player.softQueue("essence_mine_exit", 3) {
        val npc = player["last_npc_teleport_to_rune_essence_mine", "sedridor"]
        val tile = teleports[npc]
        player.tele(tile)
    }
}