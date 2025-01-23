package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.death.playerDeath
import world.gregs.voidps.world.interact.entity.item.destroyed
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()

objectOperate("Search", "rocks_skull_restless_ghost_quest") {
    if (player.quest("the_restless_ghost") != "mining_spot" && player.quest("the_restless_ghost") != "found_skull") {
        player.message("There's nothing there of any use to you.")
        return@objectOperate
    }
    if (player.inventory.isFull()) {
        player.message("You can see the skull under the rocks, but you don't have enough space to carry it.")
        return@objectOperate
    }
    statement("You take the skull from the pile of rocks.")
    player.inventory.add("muddy_skull")
    player["rocks_restless_ghost"] = "no_skull"
    player["the_restless_ghost"] = "found_skull"
    val index: Int? = player.remove("restless_ghost_warlock")
    if (index != null) {
        val skeleton = npcs.indexed(index)
        if (skeleton != null) {
            npcs.removeIndex(skeleton)
            npcs.remove(skeleton)
        }
    }
    player.message("A skeleton warlock has appeared.")
    val warlock = npcs.add("skeleton_warlock", Tile(3236, 3149), Direction.SOUTH) ?: return@objectOperate
    player["restless_ghost_warlock"] = warlock.index
    warlock.anim("restless_ghost_warlock_spawn")
    val player = player
    warlock.softQueue("delayed_attack", 4) {
        warlock.mode = Interact(warlock, player, PlayerOption(warlock, player, "Attack"))
    }
    World.queue("skeleton_warlock", TimeUnit.SECONDS.toTicks(60)) {
        npcs.remove(warlock)
        npcs.removeIndex(warlock)
        player.clear("restless_ghost_warlock")
    }
}

objectOperate("Search", "rocks_no_skull_restless_ghost_quest") {
    if (player.quest("the_restless_ghost") == "completed") {
        player.message("There's nothing of any interest.")
    } else {
        player.message("You already have the ghost's skull.")
    }
}

playerDeath { player ->
    if (!player.ownsItem("muddy_skull")) {
        player["rocks_restless_ghost"] = "skull"
    }
}

destroyed("muddy_skull") { player ->
    player["rocks_restless_ghost"] = "skull"
}