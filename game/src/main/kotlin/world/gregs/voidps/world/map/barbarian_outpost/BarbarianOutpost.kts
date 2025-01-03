package world.gregs.voidps.world.map.barbarian_outpost

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.entity.obj.door.Door

objectOperate("Open", "barbarian_outpost_gate") {
    if (player.questComplete("alfred_grimhands_barcrawl")) {
        Door.enter(player, target)
    } else {
        // TODO
    }
}