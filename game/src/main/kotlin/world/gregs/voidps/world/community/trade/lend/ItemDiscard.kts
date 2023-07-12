package world.gregs.voidps.world.community.trade.lend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.community.trade.lend.Loan.getExpiry
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

/**
 * Lent item discarding
 */

val logger = InlineLogger()

on<InventoryOption>({ inventory == "inventory" && option == "Discard" }) { player: Player ->
    if (!player.contains("borrowed_item")) {
        if (player.inventory.clear(slot)) {
            logger.info { "$player discarded un-borrowed item $item" }
        }
        return@on
    }
    item("""
        <col=00007f>~ Loan expires ${getExpiryMessage(player)} ~</col>
        If you discard this item, it will disappear.
        You won't be able to pick it up again.
    """, get<ItemDefinitions>().get(item.def.lendId).stringId, 900)

    choice("Really discard item?") {
        option("Yes, discard it. I won't need it again.") {
            player.message("The item has been returned to it's owner.")
            player.inventory.clear(slot)
            player.clear("borrowed_item")
            player.clear("borrowed_from")
            player.clear("borrow_timeout")
            player.softTimers.clear("borrow_message")
        }
        option("No, I'll keep hold of it.")
    }
}

fun getExpiryMessage(player: Player): String {
    return if (player.contains("borrow_timeout")) {
        getExpiry(player, "borrow_timeout")
    } else {
        "after logout"
    }
}