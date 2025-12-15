package content.skill.farming

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import kotlin.text.replace

class Sack : Script {

    val logger = InlineLogger()

    private val vegetables = mapOf(
        "raw_potato" to "potatoes",
        "onion" to "onions",
        "cabbage" to "cabbages",
    )

    init {
        itemOption("Fill", "empty_sack") {
            var index = -1
            for (id in vegetables.keys) {
                index = inventory.indexOf(id)
                if (index != -1) {
                    break
                }
            }

            if (index == -1) {
                message("You don't have any potatoes, onions or cabbages.")
                return@itemOption
            }
            val item = inventory[index]
            val plural = vegetables[item.id]
            if (plural == null) {
                message("You don't have any potatoes, onions or cabbages.")
                return@itemOption
            }
            inventory.transaction {
                val removed = removeToLimit(item.id, 10)
                if (removed == 0) {
                    error = TransactionError.Deficient(0)
                }
                replace(it.slot, "empty_sack", "${plural}_$removed")
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> message("You don't have any potatoes, onions or cabbages.")
                TransactionError.None -> {}
                else -> logger.warn { "Error filling sack" }
            }
        }

        itemOption("Fill", "potatoes_10,onions_10,cabbages_10") { (item) ->
            val id = item.id.substringBeforeLast("_").removeSuffix("es").removeSuffix("s")
            message("The $id sack is already full.")
        }

        // Potatoes

        itemOnItem("raw_potato", "potatoes_10") { _, _ ->
            message("The potato sack is already full.")
        }

        itemOption("Fill", "potatoes_#") { (item, slot) ->
            fill(item, slot, "raw_potato")
        }

        itemOption("Remove-one", "potatoes_*") { (item, slot) ->
            removeOne(item, slot, "raw_potato", "potato")
        }

        itemOption("Empty", "potatoes_*") { (item, slot) ->
            empty(item, slot, "raw_potato")
        }

        // Onions

        itemOnItem("onion", "onions_10") { _, _ ->
            message("The onion sack is already full.")
        }

        itemOption("Fill", "onions_#") { (item, slot) ->
            fill(item, slot, "onion")
        }

        itemOption("Remove-one", "onions_*") { (item, slot) ->
            removeOne(item, slot, "onion")
        }

        itemOption("Empty", "onions_*") { (item, slot) ->
            empty(item, slot, "onion")
        }

        // Cabbages

        itemOnItem("cabbage", "cabbages_10") { _, _ ->
            message("The cabbage sack is already full.")
        }

        itemOption("Fill", "cabbages_#") { (item, slot) ->
            fill(item, slot, "cabbage")
        }

        itemOption("Remove-one", "cabbages_*") { (item, slot) ->
            removeOne(item, slot, "cabbage")
        }

        itemOption("Empty", "cabbages_*") { (item, slot) ->
            empty(item, slot, "cabbage")
        }
    }

    fun Player.fill(item: Item, slot: Int, id: String) {
        val current = item.id.substringAfterLast("_").toInt()
        inventory.transaction {
            val removed = removeToLimit(id, 10 - current)
            if (removed == 0) {
                error = TransactionError.Deficient(0)
            }
            replace(slot, item.id, item.id.replace("_${current}", "_${current + removed}"))
        }

        when (inventory.transaction.error) {
            is TransactionError.Deficient -> {}
            TransactionError.None -> {}
            else -> logger.warn { "Error filling $id." }
        }
    }

    fun Player.removeOne(item: Item, slot: Int, id: String, name: String = id) {
        val plural = item.id.substringBeforeLast("_")
        val current = item.id.removePrefix("${plural}_").toInt()
        inventory.transaction {
            add(id)
            replace(slot, item.id, if (current == 1) "empty_sack" else "${plural}_${current - 1}")
        }
        when (inventory.transaction.error) {
            is TransactionError.Full -> inventoryFull()
            TransactionError.None -> message("You take ${id.an()} $plural out of the $name sack.")
            else -> logger.warn { "Error emptying ${plural}." }
        }
    }

    fun Player.empty(item: Item, slot: Int, id: String) {
        val plural = item.id.substringBeforeLast("_")
        val current = item.id.removePrefix("${plural}_").toInt()

        inventory.transaction {
            val added = addToLimit(id, current)
            if (added == 0) {
                error = TransactionError.Full(0)
            }
            replace(slot, item.id, if (added == current) "empty_sack" else "${plural}_${current - added}")
        }

        when (inventory.transaction.error) {
            is TransactionError.Full -> inventoryFull()
            TransactionError.None -> {}
            else -> logger.warn { "Error emptying ${plural}." }
        }
    }
}
