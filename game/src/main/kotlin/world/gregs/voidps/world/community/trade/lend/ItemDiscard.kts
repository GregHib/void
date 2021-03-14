package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.has
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.community.trade.lend.Loan.getExpiry
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.entity.player.equip.ContainerAction

/**
 * Lent item discarding
 */

val decoder: ItemDefinitions by inject()
val logger = InlineLogger()

ContainerAction where { container == "inventory" && option == "Discard" } then {
    val id = player.inventory.getItem(slot)
    val amount = player.inventory.getAmount(slot)
    if (!player.has("borrowed_item")) {
        if (player.inventory.clear(slot)) {
            logger.info { "$player discarded un-borrowed item $id $amount" }
        }
        return@then
    }
    val def = decoder.get(id)
    player.dialogue {
        item("""
                <col=00007f>~ Loan expires ${getExpiryMessage(player)} ~</col>
                If you discard this item, it will disappear.
                You won't be able to pick it up again.
            """, def.lendId, 900)

        val discard = choice("""
                Yes, discard it. I won't need it again.
                No, I'll keep hold of it.
            """, "Really discard item?", saySelection = false)

        if (discard == 1) {
            player.message("The item has been returned to it's owner.")
            returnLoan(player, id)
            if (player.inventory.clear(slot)) {
                logger.info { "$player discarded item $id $amount" }
            } else {
                logger.info { "Error discarding item $id $amount for $player" }
            }
        }
    }
}

fun getExpiryMessage(player: Player): String {
    return if (!player.has("borrow_timeout")) {
        getExpiry(player, "borrow_timeout")
    } else {
        "after logout"
    }
}