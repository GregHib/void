package content.skill.woodcutting

import content.entity.player.inv.inventoryOptions
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

val drops: DropTables by inject()
val itemDefinitions: ItemDefinitions by inject()

inventoryOptions("Search", "inventory") {
    val tableId = when {
        item.id == "birds_nest_ring" -> "birds_nest_ring_table"
        item.id == "birds_nest_seeds_1" -> "birds_nest_seed_table"
        item.id == "birds_nest_seeds_2" -> "birds_nest_cheap_seed_table"
        item.id == "birds_nest_red_egg" -> "birds_nest_egg_red_table"
        item.id == "birds_nest_green_egg" -> "birds_nest_egg_green_table"
        item.id == "birds_nest_blue_egg" -> "birds_nest_egg_blue_table"
        item.id == "birds_nest_raven_egg" -> "birds_nest_egg_raven_table"
        else -> return@inventoryOptions
    }

    val table = drops.get(tableId) ?: return@inventoryOptions
    val items = mutableListOf<ItemDrop>()
    table.role(list = items)

    val drop = items.firstOrNull() ?: return@inventoryOptions
    val itemId = drop.id
    val itemAmount = drop.amount?.start ?: 1

    if (player.inventory.isFull()) {
        player.message("Your inventory is too full to take anything out of the bird's nest.")
        return@inventoryOptions
    }

    val itemName = itemDefinitions.get(drop.id).name.lowercase()
    if (player.inventory.replace(slot, item.id, "birds_nest_empty")) {
        player.inventory.add(itemId, itemAmount)
    }

    if (itemId.contains("acorn") || itemId.contains("orange") || itemId.contains("apple_tree") || itemId.contains("emerald")) {
        if (itemAmount > 1) {
            player.message("You take $itemAmount $itemName"+"s out of the bird's nest.")
        } else {
            player.message("You take an $itemName out of the bird's nest.")
        }
    } else {
        if (itemAmount > 1) {
            player.message("You take $itemAmount $itemName"+"s out of the bird's nest.")
        } else {
            player.message("You take a $itemName out of the bird's nest.")
        }
    }
}