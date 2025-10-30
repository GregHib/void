package content.skill.fletching

import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class HeadlessArrows : Script {

    init {
        itemOnItem("feather", "arrow_shaft") {
            if (fromItem.amount <= 15 || toItem.amount <= 15) {
                val amountToMake = minOf(fromItem.amount, toItem.amount)
                makeImmediately(it, "headless_arrow", amountToMake)
                return@itemOnItem
            }
            it.weakQueue("feather_to_shaft_dialog") {
                val (selected, amount) = makeAmount(
                    listOf("headless_arrow"),
                    type = "Make sets: ",
                    maximum = 10,
                    text = "How many sets of 15 do you wish to feather?",
                )
                makeHeadlessArrows(player, selected, amount)
            }
        }
    }

    fun makeHeadlessArrows(player: Player, addItem: String, amount: Int) {
        if (amount <= 0) {
            player.queue.clear("feather_to_shaft_create")
            return
        }

        val currentShafts = player.inventory.count("arrow_shaft")
        val currentFeathers = player.inventory.count("feather")

        val actualAmount = when {
            currentShafts < 15 || currentFeathers < 15 -> minOf(currentShafts, currentFeathers)
            else -> 15
        }

        if (actualAmount < 1) {
            player.message("You don't have enough materials to fletch headless arrows.", ChatType.Game)
            return
        }

        player.weakQueue("feather_to_shaft_create", 2) {
            val success = player.inventory.transaction {
                remove("feather", actualAmount)
                remove("arrow_shaft", actualAmount)
                add(addItem, actualAmount)
            }
            if (!success) {
                return@weakQueue
            }
            val experiencePerArrow = 15.0 / 15
            val totalExperience = experiencePerArrow * actualAmount
            player.experience.add(Skill.Fletching, totalExperience)
            player.message("You attach feathers to $actualAmount arrow shafts.")
            makeHeadlessArrows(player, addItem, amount - 1)
        }
    }

    fun makeImmediately(player: Player, addItem: String, amount: Int) {
        player.weakQueue("feather_to_shaft_create", 2) {
            val success = player.inventory.transaction {
                remove("feather", amount)
                remove("arrow_shaft", amount)
                add(addItem, amount)
            }
            if (!success) {
                return@weakQueue
            }
            val experiencePerArrow = 15.0 / 15
            val totalExperience = experiencePerArrow * amount
            player.experience.add(Skill.Fletching, totalExperience)
            player.message("You attach feathers to $amount arrow shafts.")
        }
    }
}
