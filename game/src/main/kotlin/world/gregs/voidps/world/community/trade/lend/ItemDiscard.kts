package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.community.trade.lend.Loan.getExpiry
import world.gregs.voidps.world.community.trade.lend.Loan.returnLoan
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

/**
 * Lent item discarding
 */

val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && option == "Discard" }) { player: Player ->
    if (!player.contains("borrowed_item")) {
        if (player.inventory.clear(slot)) {
            logger.info { "$player discarded un-borrowed item $item" }
        }
        return@on
    }
    player.dialogue {
        item("""
                <col=00007f>~ Loan expires ${getExpiryMessage(player)} ~</col>
                If you discard this item, it will disappear.
                You won't be able to pick it up again.
            """, get<ItemDefinitions>().get(item.def.lendId).stringId, 900)


        val discard = choice("""
                Yes, discard it. I won't need it again.
                No, I'll keep hold of it.
            """, "Really discard item?")

        if (discard == 1) {
            player.message("The item has been returned to it's owner.")
            returnLoan(player, item.id)
            if (player.inventory.clear(slot)) {
                logger.info { "$player discarded item $item" }
            } else {
                logger.info { "Error discarding item $item for $player" }
            }
        }
    }
}

fun getExpiryMessage(player: Player): String {
    return if (!player.contains("borrow_timeout")) {
        getExpiry(player, "borrow_timeout")
    } else {
        "after logout"
    }
}