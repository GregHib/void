package world.gregs.voidps.world.map.taverley

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.interact.entity.obj.door.Door


objectOperate("Open", "door_549_closed", "door_550_closed") {
    Door.enter(player, target)
}

objectOperate("Open", "door_540_closed", "door_542_closed") {
    Door.enter(player, target)
}


//todo check if you can do it before quest and after quest
itemOnObjectOperate("raw_beef", "cauldron_of_thunder") {
    player.message("You dip the raw beef in the cauldron.")
    player.inventory.replace("raw_beef","enchanted_beef")
}
itemOnObjectOperate("raw_rat_meat", "cauldron_of_thunder") {
    player.message("You dip the raw rat meat in the cauldron.")
    player.inventory.replace("raw_rat_meat","enchanted_rat_meat")
}
itemOnObjectOperate("raw_bear_meat", "cauldron_of_thunder") {
    player.message("You dip the raw bear meat in the cauldron.")
    player.inventory.replace("raw_bear_meat","enchanted_bear_meat")
}
itemOnObjectOperate("raw_chicken", "cauldron_of_thunder") {
    player.message("You dip the raw chicken in the cauldron.")
    player.inventory.replace("raw_chicken","enchanted_chicken")
}