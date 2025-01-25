package content.skill.runecrafting

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItems
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import content.entity.player.inv.item.dropped
import content.entity.player.inv.inventoryItem

val pouches = arrayOf("small_pouch", "medium_pouch", "medium_pouch_damaged", "large_pouch", "large_pouch_damaged", "giant_pouch", "giant_pouch_damaged")

inventoryItem("Check", *pouches) {
    val essence = player["${item.id.removeSuffix("_damaged")}_essence", 0]
    if (essence == 0) {
        player.message("There is no essence in this pouch.")
        return@inventoryItem
    }
    val number = numberString(essence)
    val pure = player["${item.id.removeSuffix("_damaged")}_pure", false]
    player.message("There are $number ${if (pure) "pure" else "rune"} ${"essence".plural(essence)} in this pouch.")
}

inventoryItem("Fill", *pouches) {
    val id = item.id.removeSuffix("_damaged")
    val maximum = capacity(item.id, player.inventory.charges(player, slot))
    val essence = player["${id}_essence", 0]
    if (essence >= maximum) {
        player.message("You cannot add any more essence to the pouch.")
        return@inventoryItem
    }
    var removed = 0
    var pure = true
    if (essence != 0) {
        pure = player["${id}_pure", false]
        val limit = maximum - essence
        val success = player.inventory.transaction {
            removed = removeToLimit("${if (pure) "pure" else "rune"}_essence", limit)
        }
        if (!success) {
            return@inventoryItem
        }
        player["${id}_essence"] = essence + removed
        return@inventoryItem
    }
    val success = player.inventory.transaction {
        removed = removeToLimit("pure_essence", maximum)
        if (removed == 0) {
            removed = removeToLimit("rune_essence", maximum)
            pure = false
        }
    }
    if (!success || removed == 0) {
        player.message("You do not have any essence to fill your pouch with.")
        return@inventoryItem
    }
    player["${id}_essence"] = removed
    player["${id}_pure"] = pure
}

inventoryItem("Empty", *pouches) {
    val id = item.id.removeSuffix("_damaged")
    val essence = player["${id}_essence", 0]
    val pure = player["${id}_pure", false]
    if (essence == 0) {
        player.message("There is no essence in this pouch.")
        return@inventoryItem
    }

    var added = 0
    val success = player.inventory.transaction {
        added = addToLimit("${if (pure) "pure" else "rune"}_essence", essence)
    }
    if (!success || added == 0) {
        player.inventoryFull()
        return@inventoryItem
    }
    if (Settings["runecrafting.pouch.degrade", true]) {
        player.inventory.discharge(player, slot)
    }
    player["${id}_essence"] = essence - added
}

itemOnItems(arrayOf("pure_essence"), pouches) { player ->
    addSingle(player, fromSlot, fromItem, toSlot, toItem)
}

itemOnItems(arrayOf("rune_essence"), pouches) { player ->
    addSingle(player, fromSlot, fromItem, toSlot, toItem)
}

dropped(*pouches) { player ->
    val id = item.id.removeSuffix("_damaged")
    if (player.clear("${id}_essence") != null) {
        player.message("The contents of the pouch fell out as you dropped it!")
    }
}

fun addSingle(
    player: Player,
    fromSlot: Int,
    fromItem: Item,
    toSlot: Int,
    toItem: Item
) {
    val id = toItem.id.removeSuffix("_damaged")
    val desired = fromItem.id.startsWith("pure")
    val pure = player["${id}_pure", false]
    if (pure != desired) {
        val name = if (pure) "pure" else "normal"
        player.message("This pouch contains $name essence, so you can only fill it with more $name essence.")
        return
    }
    val maximum = capacity(toItem.id, player.inventory.charges(player, toSlot))
    val essence = player["${id}_essence", 0]
    if (essence >= maximum) {
        player.message("You cannot add any more essence to the pouch.")
        return
    }
    val success = player.inventory.transaction {
        remove(fromSlot, fromItem.id)
    }
    if (!success) {
        return
    }
    if (essence == 0) {
        player["${id}_pure"] = desired
    }
    player["${id}_essence"] = essence + 1
}

private fun capacity(id: String, charges: Int) = when (id) {
    "medium_pouch" -> 6
    "medium_pouch_damaged" -> when {
        // TODO proper values
        charges < 10 -> 1
        charges < 15 -> 2
        else -> 3
    }
    "large_pouch" -> 9
    "large_pouch_damaged" -> when {
        // TODO proper values
        charges < 4 -> 3
        charges < 6 -> 4
        charges < 10 -> 5
        else -> 7
    }
    "giant_pouch" -> 12
    "giant_pouch_damaged" -> when {
        // TODO proper values
        charges < 5 -> 3
        charges < 10 -> 4
        charges < 15 -> 8
        else -> 9
    }
    else -> 3
}

private fun numberString(essence: Int) = when (essence) {
    1 -> "one"
    2 -> "two"
    3 -> "three"
    4 -> "four"
    5 -> "five"
    6 -> "six"
    7 -> "seven"
    8 -> "eight"
    9 -> "nine"
    10 -> "ten"
    11 -> "eleven"
    12 -> "twelve"
    else -> "zero"
}