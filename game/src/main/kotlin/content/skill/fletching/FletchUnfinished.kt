package content.skill.fletching

import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class FletchUnfinished : Script {

    init {
        @Suppress("UNCHECKED_CAST")
        itemOnItem("knife", "*logs*") { _, toItem ->
            val displayItems = toItem.def.extras?.get("fletchables") as? List<String> ?: return@itemOnItem
            weakQueue("fletching_make_dialog") {
                val (selected, amount) = makeAmount(
                    displayItems,
                    type = "Make",
                    maximum = 27,
                    text = "What would you like to fletch?",
                )
                EnumDefinitions.intOrNull("unf_fletching_xp", selected) ?: return@weakQueue
                val level = EnumDefinitions.int("unf_fletching_level", selected)
                if (!has(Skill.Fletching, level, true)) {
                    return@weakQueue
                }
                fletch(selected, toItem.id, amount)
            }
        }
    }

    fun Player.fletch(addItem: String, removeItem: String, amount: Int) {
        if (amount <= 0) {
            return
        }

        if (!inventory.contains("knife") || !inventory.contains(removeItem)) {
            return
        }

        val tick = EnumDefinitions.int("unf_fletching_tick", addItem)
        weakQueue("fletching", tick) {
            val makeAmount = EnumDefinitions.int("unf_fletching_make_amount", addItem)
            val success = inventory.transaction {
                remove(removeItem)
                add(addItem, makeAmount)
            }

            if (!success) {
                return@weakQueue
            }

            val itemCreated = getFletched(addItem)
            message("You carefully cut the wood into $itemCreated.", ChatType.Game)
            val xp = EnumDefinitions.int("unf_fletching_xp", addItem)
            experience.add(Skill.Fletching, xp / 10.0)
            anim("fletching_log")
            fletch(addItem, removeItem, amount - 1)
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
