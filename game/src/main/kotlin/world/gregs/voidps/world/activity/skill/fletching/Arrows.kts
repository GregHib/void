package world.gregs.voidps.world.activity.skill.fletching

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount



itemOnItem("headless_arrow", "*_arrowtips") {
    it.weakQueue("feather_to_shaft_dialog") {
        val (selected, amount) = makeAmount(
                listOf("headless_arrow"),
                type = "Make sets: ",
                maximum = 10,
                text = "How many sets of 15 do you wish to feather?"
        )
        makeHeadlessArrows(player, selected, amount)
    }
}



itemOnItem("feather", "arrow_shaft") {
    if(fromItem.amount <= 15 || toItem.amount <= 15) {
        val amountToMake = minOf(fromItem.amount, toItem.amount)
        makeImmediately(it, "headless_arrow", amountToMake)
        return@itemOnItem
    }
    it.weakQueue("feather_to_shaft_dialog") {
        val (selected, amount) = makeAmount(
                listOf("headless_arrow"),
                type = "Make sets: ",
                maximum = 10,
                text = "How many sets of 15 do you wish to feather?"
        )
        makeHeadlessArrows(player, selected, amount)
    }
}

fun makeHeadlessArrows(player: Player, addItem: String, amount: Int) {
    if (amount <= 0) {
        player.softTimers.stop("feather_to_shaft_create")
        return
    }

    val currentShafts = player.inventory.count("arrow_shaft")
    val currentFeathers = player.inventory.count("feather")

    val actualAmount = when {
        currentShafts < 10 || currentFeathers < 10 -> minOf(currentShafts, currentFeathers)
        else -> 10
    }

    if (actualAmount < 1) {
        return
    }

    player.weakQueue("feather_to_shaft_create", 2) {
        player.inventory.transaction {
            player.setAnimation("feather_to_shaft")
            remove("feather", actualAmount)
            remove("arrow_shaft", actualAmount)
            add(addItem, actualAmount)
            val experiencePerArrow = 15.0 / 10
            val totalExperience = experiencePerArrow * actualAmount
            player.experience.add(Skill.Fletching, totalExperience)
            player.message("You attach feathers to $actualAmount arrow shafts.")
        }
        makeHeadlessArrows(player, addItem, amount - 1)
    }
}


fun makeImmediately(player: Player, addItem: String, amount: Int) {
    player.weakQueue("feather_to_shaft_create", 2) {
        player.inventory.transaction {
            player.setAnimation("feather_to_shaft")
            remove("feather", amount)
            remove("arrow_shaft", amount)
            add(addItem, amount)
            player.experience.add(Skill.Fletching, 15.0)
            player.message("You attach feathers to $amount arrow shafts.")
        }
    }
}