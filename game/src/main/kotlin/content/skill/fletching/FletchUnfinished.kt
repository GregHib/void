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
        itemOnItem("knife", "*logs*") { _, toItem ->
            fletchLogDialog(toItem.id)
        }
    }
}

/**
 * Opens the "What would you like to fletch?" make-menu for [logId] and produces the chosen unfinished
 * item. [hasTool] is re-checked before every cut - a knife by default; the beaver additionally counts
 * as a knife while it's actively cutting. [onMissingTool] fires (and fletching stops) when a cut can't
 * proceed. [animate] plays the per-cut animation (the player's knife-cut by default).
 */
fun Player.fletchLogDialog(
    logId: String,
    animate: Player.() -> Unit = { anim("fletching_log") },
    hasTool: Player.() -> Boolean = { inventory.contains("knife") },
    onMissingTool: Player.() -> Unit = { message("You need a knife to do that.") },
) {
    val displayItems = Tables.itemListOrNull("fletchables.$logId.products") ?: return
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
        fletchLog(selected, unf, logId, amount, animate, hasTool, onMissingTool)
    }
}

fun Player.fletchLog(
    addItem: String,
    unf: RowDefinition,
    removeItem: String,
    amount: Int,
    animate: Player.() -> Unit = { anim("fletching_log") },
    hasTool: Player.() -> Boolean = { inventory.contains("knife") },
    onMissingTool: Player.() -> Unit = { message("You need a knife to do that.") },
) {
    if (amount <= 0 || !inventory.contains(removeItem)) {
        return
    }

    // Re-checked each cut: if the tool is gone (e.g. the beaver finished cutting mid-batch), stop.
    if (!hasTool()) {
        onMissingTool()
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
        animate()
        fletchLog(addItem, unf, removeItem, amount - 1, animate, hasTool, onMissingTool)
    }
}

fun getFletched(itemName: String): String = when {
    itemName.contains("shortbow", ignoreCase = true) -> "a Shortbow"
    itemName.contains("longbow", ignoreCase = true) -> "a Longbow"
    itemName.contains("stock", ignoreCase = true) -> "a Stock"
    itemName.contains("shaft", ignoreCase = true) -> "Shafts"
    else -> "null"
}
