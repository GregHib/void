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

class Baskets : Script {

    val logger = InlineLogger()

    private data class Fruit(val name: String, val plural: String, val description: String = "a $name")

    private val fruit = mapOf(
        "strawberry" to Fruit("strawberry", "strawberries"),
        "orange" to Fruit("orange", "oranges", "an orange"),
        "banana" to Fruit("banana", "bananas"),
        "cooking_apple" to Fruit("apple", "apples", "an apple"),
        "tomato" to Fruit("tomato", "tomatoes"),
    )

    init {
        itemOption("Fill", "basket") {
            var index = -1
            for (id in fruit.keys) {
                index = inventory.indexOf(id)
                if (index != -1) {
                    break
                }
            }

            if (index == -1) {
                message("You don't have any fruit with which to fill the basket.")
                return@itemOption
            }
            val item = inventory[index]
            val veg = fruit[item.id]
            if (veg == null) {
                message("You don't have any fruit with which to fill the basket.")
                return@itemOption
            }
            inventory.transaction {
                val removed = removeToLimit(item.id, 5)
                if (removed == 0) {
                    error = TransactionError.Deficient(0)
                }
                replace(it.slot, "basket", "${veg.plural}_$removed")
            }
            when (inventory.transaction.error) {
                is TransactionError.Deficient -> message("You don't have any fruit with which to fill the basket.")
                TransactionError.None -> {}
                else -> logger.warn { "Error filling fruit basket." }
            }
        }

        for ((id, fruit) in fruit) {
            itemOption("Fill", "${fruit.plural}_#") { (item, slot) ->
                val current = item.id.removePrefix("${fruit.plural}_").toInt()
                inventory.transaction {
                    val removed = removeToLimit(id, 5 - current)
                    if (removed == 0) {
                        error = TransactionError.Deficient(0)
                    }
                    replace(slot, item.id, "${fruit.plural}_${current + removed}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> {}
                    TransactionError.None -> {}
                    else -> logger.warn { "Error filling ${fruit.plural}." }
                }
            }

            itemOption("Fill", "${fruit.plural}_5") {
                message("The ${fruit.name} basket is already full.")
            }

            itemOnItem(id, "${fruit.plural}_5") { _, _ ->
                message("The ${fruit.name} basket is already full.")
            }

            itemOption("Remove-one", "${fruit.plural}_#") { (item, slot) ->
                val current = item.id.removePrefix("${fruit.plural}_").toInt()

                inventory.transaction {
                    add(id)
                    replace(slot, item.id, if (current == 1) "basket" else "${fruit.plural}_${current - 1}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Full -> inventoryFull()
                    TransactionError.None -> message("You take ${fruit.description} out of the ${fruit.name} basket.")
                    else -> logger.warn { "Error emptying ${fruit.description}." }
                }
            }

            itemOption("Empty", "${fruit.plural}_#") { (item, slot) ->
                val current = item.id.removePrefix("${fruit.plural}_").toInt()

                inventory.transaction {
                    val added = addToLimit(id, current)
                    if (added == 0) {
                        error = TransactionError.Full(0)
                    }
                    replace(slot, item.id, if (added == current) "basket" else "${fruit.plural}_${current - added}")
                }

                when (inventory.transaction.error) {
                    is TransactionError.Full -> inventoryFull()
                    TransactionError.None -> {}
                    else -> logger.warn { "Error emptying ${fruit.plural}." }
                }
            }
        }
    }
}
