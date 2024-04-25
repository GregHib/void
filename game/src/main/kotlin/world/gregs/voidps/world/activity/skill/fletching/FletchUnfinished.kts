package world.gregs.voidps.world.activity.skill.fletching

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

itemOnItem("knife", "*logs*") {
    val usedLog = if (isfletchableLog(toItem.id)) toItem else fromItem
    val displayItems = (toItem.def.extras as? MutableMap<String, Any>)?.remove("fletchables") as? List<String> ?: return@itemOnItem

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