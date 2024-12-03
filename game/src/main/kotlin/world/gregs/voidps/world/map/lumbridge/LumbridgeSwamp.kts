package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.item.canDestroy


objectOperate("Search", "rocks_skull_restless_ghost_quest") {
    if (player.quest("the_restless_ghost") == "mining_spot" || player.quest("the_restless_ghost") == "found_skull") {
        if (player.inventory.isFull()) {
            player.message("You can see the skull under the rocks, but you don't have enough space to carry it.")
        } else {
            statement("You take the skull from the pile of rocks.")
            player.inventory.add("muddy_skull")
            player["rocks_restless_ghost"] = "no_skull"
            player["the_restless_ghost"] = "found_skull"


            //player.message("A skeleton warlock has appeared.")
            //Climb out of ground 1552
            // val skeleton_warlock = npcs.add("skeleton_warlock", Tile(3236, 3149), Direction.SOUTH) ?: return@objectOperate
            // World.queue("skeleton_warlock", TimeUnit.SECONDS.toTicks(60)) {
            //     npcs.removeIndex(skeleton_warlock)
            // }

        }
    } else {
        player.message("There's nothing there of any use to you.")
    }
}

objectOperate("Search", "rocks_no_skull_restless_ghost_quest") {
    if (player.quest("the_restless_ghost") == "completed") {
        player.message("There's nothing of any interest.")
    } else {
        player.message("You already have the ghost's skull.")
    }
}

canDestroy("muddy_skull") { player ->
    player["rocks_restless_ghost"] = "skull"
}