package rs.dusk.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.has
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.community.trade.lend.Loan.getExpiry
import rs.dusk.world.community.trade.lend.Loan.returnLoan
import rs.dusk.world.interact.dialogue.type.choice
import rs.dusk.world.interact.dialogue.type.item
import rs.dusk.world.interact.entity.player.equip.ContainerAction

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