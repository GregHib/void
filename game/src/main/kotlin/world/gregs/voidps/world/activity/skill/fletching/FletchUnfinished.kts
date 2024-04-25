package world.gregs.voidps.world.activity.skill.fletching

import com.github.michaelbull.logging.InlineLogger
import org.slf4j.MDC.remove
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Fletching
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val itemDefinitions: ItemDefinitions by inject()
val logger = InlineLogger()

itemOnItem("knife", "*logs*") {
    val usedLog = if (isfletchableLog(toItem.id)) toItem else fromItem
    val displayItems = getFletchItems(usedLog.id)

    if(displayItems.isEmpty()) {
        logger.warn { "${usedLog.id} doesn't have any selectable items for the make interface." }
        return@itemOnItem
    }

    it.weakQueue("fletching_make_dialog") {
        val (selected, amount) = makeAmount(
            displayItems,
            type = "Make",
            maximum = 27,
            text = "What would you like to fletch?"
        )
        val itemToFletch: Fletching = itemDefinitions.get(selected).getOrNull("fletching_unf") ?: return@weakQueue
        if (!it.has(Skill.Fletching, itemToFletch.level, true)) {
            return@weakQueue
        }
        fletch(it, selected, itemToFletch, usedLog.id, amount)
    }
}

fun fletch(player: Player, addItem: String, addItemDef: Fletching, removeItem: String, amount: Int) {
    if (amount <= 0) {
        player.softTimers.stop("fletching")
        return
    }

    if(!player.inventory.contains("knife") || !player.inventory.contains(removeItem)) {
        player.softTimers.stop("fletching")
        return
    }

    player.weakQueue("fletching", addItemDef.tick) {
        val success = player.inventory.transaction {
            remove(removeItem)
            add(addItem, addItemDef.makeAmount)
        }

        if(!success) {
            return@weakQueue
        }

        val itemCreated = getFletched(addItem)
        player.message("You carefully cut the wood into a $itemCreated", ChatType.Game)
        player.experience.add(Skill.Fletching, addItemDef.xp)
        player.setAnimation(addItemDef.animation)
        fletch(player, addItem, addItemDef, removeItem, amount -1)
    }
}

fun getFletchItems(log: String): List<String> {
    return when (log) {
        "logs" -> listOf("arrow_shaft", "shortbow_u", "longbow_u", "wooden_stock")
        "oak_logs" -> listOf("arrow_shaft", "oak_shortbow_u", "oak_longbow_u", "oak_stock")
        "willow_logs" -> listOf("arrow_shaft", "willow_shortbow_u", "willow_longbow_u", "willow_stock")
        "maple_logs" -> listOf("arrow_shaft", "maple_shortbow_u", "maple_longbow_u", "maple_stock")
        "teak_logs" -> listOf("arrow_shaft", "teak_stock")
        "mahogany_logs" -> listOf("arrow_shaft", "mahogany_stock")
        "yew_logs" -> listOf("arrow_shaft", "yew_shortbow_u", "yew_longbow_u", "yew_stock")
        "magic_logs" -> listOf("arrow_shaft", "magic_shortbow_u", "magic_longbow_u")
        else -> emptyList()
    }
}

fun getFletched(itemName: String): String {
    return when {
        itemName.contains("shortbow", ignoreCase = true) -> "Shortbow."
        itemName.contains("longbow", ignoreCase = true) -> "Longbow."
        itemName.contains("stock", ignoreCase = true) -> "Stock."
        else -> "Null"
    }
}

fun isfletchableLog(id: String) : Boolean {
    return when (id) {
        "logs" -> true
        "oak_logs" -> true
        "willow_logs" -> true
        "maple_logs" -> true
        "teak_logs" -> true
        "mahogany_logs" -> true
        "yew_logs" -> true
        "magic_logs" -> true
        else -> false
    }
}