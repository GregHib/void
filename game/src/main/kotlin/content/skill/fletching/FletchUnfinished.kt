package content.skill.fletching

import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class FletchUnfinished : Script {

    init {
        @Suppress("UNCHECKED_CAST")
        itemOnItem("knife", "*logs*") { _, toItem ->
            val displayItems = Tables.itemListOrNull("fletchables.${toItem.id}.products") ?: return@itemOnItem
            weakQueue("fletching_make_dialog") {
                val (selected, amount) = makeAmount(
                    displayItems,
                    type = "Make",
                    maximum = 27,
                    text = "What would you like to fletch?",
                )
                val unf = Rows.getOrNull("fletching_unf.$selected") ?: return@weakQueue
                if (!has(Skill.Fletching, unf.int("level"), true)) {
                    return@weakQueue
                }
                fletch(selected, unf, toItem.id, amount)
            }
        }
    }

    fun Player.fletch(addItem: String, unf: RowDefinition, removeItem: String, amount: Int) {
        if (amount <= 0) {
            return
        }

        if (!inventory.contains("knife") || !inventory.contains(removeItem)) {
            return
        }

        weakQueue("fletching", unf.int("ticks")) {
            val makeAmount = unf.int("amount")
            val success = inventory.transaction {
                remove(removeItem)
                add(addItem, makeAmount)
            }

            if (!success) {
                return@weakQueue
            }

            val itemCreated = getFletched(addItem)
            message("You carefully cut the wood into $itemCreated.", ChatType.Game)
            val xp = unf.int("xp") / 10.0
            exp(Skill.Fletching, xp)
            anim("fletching_log")
            fletch(addItem, unf, removeItem, amount - 1)
        }
    }

    fun getFletched(itemName: String): String = when {
        itemName.contains("shortbow", ignoreCase = true) -> "a Shortbow"
        itemName.contains("longbow", ignoreCase = true) -> "a Longbow"
        itemName.contains("stock", ignoreCase = true) -> "a Stock"
        itemName.contains("shaft", ignoreCase = true) -> "Shafts"
        else -> "null"
    }
}
