package content.area.misthalin.lumbridge.roddecks_house

import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

objectOperate("Search", "roddecks_bookcase") {
    if (player.inventory.contains("roddecks_diary") && player.inventory.contains("manual_unstable_foundations")) {
        player.message("There's nothing particularly interesting here.")
    } else {
        if (!player.inventory.contains("roddecks_diary")) {
            player.inventory.add("roddecks_diary")
        }
        if (!player.inventory.contains("manual_unstable_foundations")) {
            player.inventory.add("manual_unstable_foundations")
        }
    }
}