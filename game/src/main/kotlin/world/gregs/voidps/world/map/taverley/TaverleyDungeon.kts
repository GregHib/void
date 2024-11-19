package world.gregs.voidps.world.map.taverley

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.entity.obj.door.Door


objectOperate("Open", "door_549_closed", "door_550_closed") {
    Door.enter(player, target)
}

objectOperate("Open", "door_540_closed", "door_542_closed") {
    Door.enter(player, target)
}


itemOnObjectOperate("raw_beef", "cauldron_of_thunder") {
    dip(item.id)
}

itemOnObjectOperate("raw_rat_meat", "cauldron_of_thunder") {
    dip(item.id)
}

itemOnObjectOperate("raw_bear_meat", "cauldron_of_thunder") {
    dip(item.id)
}

itemOnObjectOperate("raw_chicken", "cauldron_of_thunder") {
    dip(item.id)
}

fun ItemOnObject.dip(required: String) {
    if (player.quest("druidic_ritual") == "cauldron") {
        if (player.inventory.replace(required, required.replace("raw_", "enchanted_"))) {
            player.message("You dip the ${required.toLowerSpaceCase()} in the cauldron.")
        }
    } else {
        player.noInterest()
    }
}