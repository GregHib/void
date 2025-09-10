package content.skill.farming

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.event.Script
@Script
class Sack {

    val logger = InlineLogger()
    
    init {
        inventoryItem("Fill", "empty_sack") {
            var index = -1
            for (id in vegetables.keys) {
                index = player.inventory.indexOf(id)
                if (index != -1) {
                    break
                }
            }
        
            if (index == -1) {
                player.message("You don't have any potatoes, onions or cabbages.")
                return@inventoryItem
            }
            val item = player.inventory[index]
            val veg = vegetables[item.id]
            if (veg == null) {
                player.message("You don't have any potatoes, onions or cabbages.")
                return@inventoryItem
            }
            player.inventory.transaction {
                val removed = removeToLimit(item.id, 10)
                if (removed == 0) {
                    error = TransactionError.Deficient(0)
                }
                replace(slot, "empty_sack", "${veg.plural}_$removed")
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Deficient -> player.message("You don't have any potatoes, onions or cabbages.")
                TransactionError.None -> {}
                else -> logger.warn { "Error filling sack" }
            }
        }

        for ((id, veg) in vegetables) {
            inventoryItem("Fill", "${veg.plural}_#") {
                val current = item.id.removePrefix("${veg.plural}_").toInt()
                player.inventory.transaction {
                    val removed = removeToLimit(id, 10 - current)
                    if (removed == 0) {
                        error = TransactionError.Deficient(0)
                    }
                    replace(slot, item.id, "${veg.plural}_${current + removed}")
                }
        
                when (player.inventory.transaction.error) {
                    is TransactionError.Deficient -> {}
                    TransactionError.None -> {}
                    else -> logger.warn { "Error filling ${veg.plural}." }
                }
            }
        
            inventoryItem("Fill", "${veg.plural}_10") {
                player.message("The ${veg.name} sack is already full.")
            }
        
            itemOnItem(id, "${veg.plural}_10") { player ->
                player.message("The ${veg.name} sack is already full.")
            }
        
            inventoryItem("Remove-one", "${veg.plural}_*") {
                val current = item.id.removePrefix("${veg.plural}_").toInt()
        
                player.inventory.transaction {
                    add(id)
                    replace(slot, item.id, if (current == 1) "empty_sack" else "${veg.plural}_${current - 1}")
                }
        
                when (player.inventory.transaction.error) {
                    is TransactionError.Full -> player.inventoryFull()
                    TransactionError.None -> player.message("You take ${veg.description} out of the ${veg.name} sack.")
                    else -> logger.warn { "Error emptying ${veg.description}." }
                }
            }
        
            inventoryItem("Empty", "${veg.plural}_*") {
                val current = item.id.removePrefix("${veg.plural}_").toInt()
        
                player.inventory.transaction {
                    val added = addToLimit(id, current)
                    if (added == 0) {
                        error = TransactionError.Full(0)
                    }
                    replace(slot, item.id, if (added == current) "empty_sack" else "${veg.plural}_${current - added}")
                }
        
                when (player.inventory.transaction.error) {
                    is TransactionError.Full -> player.inventoryFull()
                    TransactionError.None -> {}
                    else -> logger.warn { "Error emptying ${veg.plural}." }
                }
            }
        }

    }

    private data class Vegetable(val name: String, val plural: String, val description: String = "a $name")
    
    private val vegetables = mapOf(
        "raw_potato" to Vegetable("potato", "potatoes"),
        "onion" to Vegetable("onion", "onions", "an onion"),
        "cabbage" to Vegetable("cabbage", "cabbages"),
    )
    
}
