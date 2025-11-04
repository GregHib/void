package content.skill.farming

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class Sack : Script {

    val logger = InlineLogger()

    private data class Vegetable(val name: String, val plural: String, val description: String = "a $name")

    private val vegetables = mapOf(
        "raw_potato" to Vegetable("potato", "potatoes"),
        "onion" to Vegetable("onion", "onions", "an onion"),
        "cabbage" to Vegetable("cabbage", "cabbages"),
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
            val veg = vegetables[item.id]
            if (veg == null) {
                message("You don't have any potatoes, onions or cabbages.")
                return@itemOption
            }
            inventory.transaction {
                val removed = removeToLimit(item.id, 10)
                if (removed == 0) {
                    error = TransactionError.Deficient(0)
                }
                replace(it.slot, "empty_sack", "${veg.plural}_$removed")
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> message("You don't have any potatoes, onions or cabbages.")
                TransactionError.None -> {}
                else -> logger.warn { "Error filling sack" }
            }
        }

        for ((id, veg) in vegetables) {
            itemOption("Fill", "${veg.plural}_#") { (item, slot) ->
                val current = item.id.removePrefix("${veg.plural}_").toInt()
                inventory.transaction {
                    val removed = removeToLimit(id, 10 - current)
                    if (removed == 0) {
                        error = TransactionError.Deficient(0)
                    }
                    replace(slot, item.id, "${veg.plural}_${current + removed}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> {}
                    TransactionError.None -> {}
                    else -> logger.warn { "Error filling ${veg.plural}." }
                }
            }

            itemOption("Fill", "${veg.plural}_10") {
                message("The ${veg.name} sack is already full.")
            }

            itemOnItem(id, "${veg.plural}_10") { _, _ ->
                message("The ${veg.name} sack is already full.")
            }

            itemOption("Remove-one", "${veg.plural}_*") { (item, slot) ->
                val current = item.id.removePrefix("${veg.plural}_").toInt()

                inventory.transaction {
                    add(id)
                    replace(slot, item.id, if (current == 1) "empty_sack" else "${veg.plural}_${current - 1}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Full -> inventoryFull()
                    TransactionError.None -> message("You take ${veg.description} out of the ${veg.name} sack.")
                    else -> logger.warn { "Error emptying ${veg.description}." }
                }
            }

            itemOption("Empty", "${veg.plural}_*") { (item, slot) ->
                val current = item.id.removePrefix("${veg.plural}_").toInt()

                inventory.transaction {
                    val added = addToLimit(id, current)
                    if (added == 0) {
                        error = TransactionError.Full(0)
                    }
                    replace(slot, item.id, if (added == current) "empty_sack" else "${veg.plural}_${current - added}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Full -> inventoryFull()
                    TransactionError.None -> {}
                    else -> logger.warn { "Error emptying ${veg.plural}." }
                }
            }
        }
    }
}
