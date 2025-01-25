package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.world.community.trade.Trade.isTrading
import content.entity.player.dialogue.type.intEntry

/**
 * Offering an item to trade or loan
 */

val definitions: ItemDefinitions by inject()

// Item must be tradeable and not lent or a dummy item
val tradeRestriction = object : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        val def = definitions.get(id)
        return def.lendTemplateId != -1 || def.singleNoteTemplateId != -1 || def.dummyItem != 0 || !def["tradeable", true]
    }
}

playerSpawn { player ->
    player.offer.itemRule = tradeRestriction
}

interfaceOption(component = "offer", id = "trade_side") {
    val amount = when (option) {
        "Offer" -> 1
        "Offer-5" -> 5
        "Offer-10" -> 10
        "Offer-All" -> Int.MAX_VALUE
        "Offer-X" -> intEntry("Enter amount:")
        else -> return@interfaceOption
    }
    offer(player, item.id, amount)
}

interfaceOption("Value", "offer", "trade_side") {
    player.message("${item.def.name} is priceless!", ChatType.Trade)
}

fun offer(player: Player, id: String, amount: Int) {
    if (!isTrading(player, amount)) {
        return
    }
    val offered = player.inventory.transaction {
        val added = removeToLimit(id, amount)
        val transaction = link(player.offer)
        transaction.add(id, added)
    }
    if (!offered) {
        player.message("That item is not tradeable.")
    }
}