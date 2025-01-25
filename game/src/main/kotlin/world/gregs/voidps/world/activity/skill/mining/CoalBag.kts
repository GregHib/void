package world.gregs.voidps.world.activity.skill.mining

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.world.interact.entity.item.canDestroy
import content.entity.player.inv.inventoryItem

val bagCapacity = 81

inventoryItem("Inspect", "coal_bag") {
    val coal = player["coal_bag_coal", 0]
    if (coal == 0) {
        player.message("Your coal bag is empty.")
    } else {
        player.message("Your coal bag has $coal ${"piece".plural(coal)} of coal in it.")
    }
}

inventoryItem("Withdraw-one", "coal_bag") {
    val coal = player["coal_bag_coal", 0]
    if (coal == 0) {
        player.message("There is no coal in your bag to withdraw.")
        return@inventoryItem
    }
    if (player.inventory.add("coal")) {
        player["coal_bag_coal"] = (coal - 1).coerceAtLeast(0)
    } else {
        player.inventoryFull()
    }
}

inventoryItem("Withdraw-many", "coal_bag") {
    val count = player["coal_bag_coal", 0]
    if (count == 0) {
        player.message("There is no coal in your bag to withdraw.")
        return@inventoryItem
    }
    val added = player.inventory.addToLimit("coal", count)
    if (added == 0) {
        return@inventoryItem
    }
    player["coal_bag_coal"] = (count - added).coerceAtLeast(0)
    player.message("You withdraw some coal.")
}

itemOnItem("coal", "coal_bag") { player ->
    val count = player.inventory.count("coal")
    if (count == bagCapacity) {
        player.message("The coal bag is already full.")
        return@itemOnItem
    }
    val coal = player["coal_bag_coal", 0]
    val limit = bagCapacity - coal
    val removed = player.inventory.removeToLimit("coal", limit)
    if (removed == 0) {
        return@itemOnItem
    }
    player["coal_bag_coal"] = (coal + removed).coerceAtMost(bagCapacity)
    player.message("You add the coal to your bag.")
}

canDestroy("coal_bag") { player ->
    val coal = player["coal_bag_coal", 0]
    if (coal > 0) {
        player.message("You can't destroy this item with coal in it.")
        cancel()
    }
}