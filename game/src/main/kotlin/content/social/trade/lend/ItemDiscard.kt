package content.social.trade.lend

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.social.trade.lend.Loan.getExpiry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemDiscard(val itemDefinitions: ItemDefinitions) : Script {

    val logger = InlineLogger()

    init {
        itemOption("Discard", "inventory") { (item, slot) ->
            if (!contains("borrowed_item")) {
                if (inventory.remove(slot, item.id)) {
                    logger.info { "$this discarded un-borrowed item $item" }
                }
                return@itemOption
            }
            val loan = itemDefinitions.get(item.def.lendId).stringId
            item(
                loan,
                900,
                """
                <col=00007f>~ Loan expires ${getExpiryMessage(this)} ~</col>
                If you discard this item, it will disappear.
                You won't be able to pick it up again.
            """,
            )

            choice("Really discard item?") {
                option("Yes, discard it. I won't need it again.") {
                    message("The item has been returned to it's owner.")
                    inventory.remove(slot, item.id)
                    clear("borrowed_item")
                    clear("borrow_timeout")
                    softTimers.clear("borrow_message")
                    val name: String? = clear("borrowed_from") as? String
                    if (name != null) {
                        val lender = Players.get(name) ?: return@option
                        lender.softTimers.stop("loan_message")
                    }
                }
                option("No, I'll keep hold of it.")
            }
        }
    }

    /**
     * Lent item discarding
     */

    fun getExpiryMessage(player: Player): String = if (player.contains("borrow_timeout")) {
        getExpiry(player, "borrow_timeout")
    } else {
        "after logout"
    }
}
