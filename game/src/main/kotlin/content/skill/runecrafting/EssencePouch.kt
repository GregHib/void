package content.skill.runecrafting

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
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

class EssencePouch : Script {

    init {
        val pouches = setOf("small_pouch", "medium_pouch", "medium_pouch_damaged", "large_pouch", "large_pouch_damaged", "giant_pouch", "giant_pouch_damaged")

        itemOption("Check", pouches.joinToString(",")) { (item) ->
            val essence = get("${item.id.removeSuffix("_damaged")}_essence", 0)
            if (essence == 0) {
                message("There is no essence in this pouch.")
                return@itemOption
            }
            val number = numberString(essence)
            val pure = get("${item.id.removeSuffix("_damaged")}_pure", false)
            message("There are $number ${if (pure) "pure" else "rune"} ${"essence".plural(essence)} in this pouch.")
        }

        itemOption("Fill", pouches.joinToString(",")) { (item, slot) ->
            val id = item.id.removeSuffix("_damaged")
            val maximum = capacity(item.id, inventory.charges(this, slot))
            val essence = get("${id}_essence", 0)
            if (essence >= maximum) {
                message("You cannot add any more essence to the pouch.")
                return@itemOption
            }
            var removed = 0
            var pure = true
            if (essence != 0) {
                pure = get("${id}_pure", false)
                val limit = maximum - essence
                val success = inventory.transaction {
                    removed = removeToLimit("${if (pure) "pure" else "rune"}_essence", limit)
                }
                if (!success) {
                    return@itemOption
                }
                set("${id}_essence", essence + removed)
                return@itemOption
            }
            val success = inventory.transaction {
                removed = removeToLimit("pure_essence", maximum)
                if (removed == 0) {
                    removed = removeToLimit("rune_essence", maximum)
                    pure = false
                }
            }
            if (!success || removed == 0) {
                message("You do not have any essence to fill your pouch with.")
                return@itemOption
            }
            set("${id}_essence", removed)
            set("${id}_pure", pure)
        }

        itemOption("Empty", pouches.joinToString(",")) { (item, slot) ->
            val id = item.id.removeSuffix("_damaged")
            val essence = get("${id}_essence", 0)
            val pure = get("${id}_pure", false)
            if (essence == 0) {
                message("There is no essence in this pouch.")
                return@itemOption
            }

            var added = 0
            val success = inventory.transaction {
                added = addToLimit("${if (pure) "pure" else "rune"}_essence", essence)
            }
            if (!success || added == 0) {
                inventoryFull()
                return@itemOption
            }
            if (Settings["runecrafting.pouch.degrade", true]) {
                inventory.discharge(this, slot)
            }
            set("${id}_essence", essence - added)
        }

        itemOnItem("pure_essence", pouches.joinToString(",")) { fromItem, toItem, fromSlot, toSlot ->
            addSingle(fromSlot, fromItem, toSlot, toItem)
        }

        itemOnItem("rune_essence", pouches.joinToString(",")) { fromItem, toItem, fromSlot, toSlot ->
            addSingle(fromSlot, fromItem, toSlot, toItem)
        }

        for (pouch in pouches) {
            dropped(pouch, ::pouchDropped)
        }
    }

    fun pouchDropped(player: Player, item: Item) {
        val id = item.id.removeSuffix("_damaged")
        if (player.clear("${id}_essence") != null) {
            player.message("The contents of the pouch fell out as you dropped it!")
        }
    }

    fun Player.addSingle(
        fromSlot: Int,
        fromItem: Item,
        toSlot: Int,
        toItem: Item,
    ) {
        val id = toItem.id.removeSuffix("_damaged")
        val desired = fromItem.id.startsWith("pure")
        val pure = get("${id}_pure", false)
        if (pure != desired) {
            val name = if (pure) "pure" else "normal"
            message("This pouch contains $name essence, so you can only fill it with more $name essence.")
            return
        }
        val maximum = capacity(toItem.id, inventory.charges(this, toSlot))
        val essence = get("${id}_essence", 0)
        if (essence >= maximum) {
            message("You cannot add any more essence to the pouch.")
            return
        }
        val success = inventory.transaction {
            remove(fromSlot, fromItem.id)
        }
        if (!success) {
            return
        }
        if (essence == 0) {
            set("${id}_pure", desired)
        }
        set("${id}_essence", essence + 1)
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
}
