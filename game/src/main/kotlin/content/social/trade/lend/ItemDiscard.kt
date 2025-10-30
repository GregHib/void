package content.social.trade.lend

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.inv.inventoryOption
import content.social.trade.lend.Loan.getExpiry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemDiscard : Script {

    val logger = InlineLogger()
    val players: Players by inject()
    val itemDefinitions: ItemDefinitions by inject()

    init {
        inventoryOption("Discard", "inventory") {
            if (!player.contains("borrowed_item")) {
                if (player.inventory.remove(slot, item.id)) {
                    logger.info { "$player discarded un-borrowed item $item" }
                }
                return@inventoryOption
            }
            val loan = itemDefinitions.get(item.def.lendId).stringId
            item(
                loan,
                900,
                """
                <col=00007f>~ Loan expires ${getExpiryMessage(player)} ~</col>
                If you discard this item, it will disappear.
                You won't be able to pick it up again.
            """,
            )

            choice("Really discard item?") {
                option("Yes, discard it. I won't need it again.") {
                    player.message("The item has been returned to it's owner.")
                    player.inventory.remove(slot, item.id)
                    player.clear("borrowed_item")
                    player.clear("borrow_timeout")
                    player.softTimers.clear("borrow_message")
                    val name: String? = player.clear("borrowed_from") as? String
                    if (name != null) {
                        val lender = players.get(name) ?: return@option
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
