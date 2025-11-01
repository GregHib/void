package content.entity.npc.shop

import content.entity.npc.shop.general.GeneralStores
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

fun Player.hasShopSample(): Boolean = this["info_sample", false]

fun Player.shop(): String = this["shop", ""]

fun Player.shopInventory(sample: Boolean = hasShopSample()): Inventory {
    val shop: String = this["shop", ""]
    val name = if (sample) "${shop}_sample" else shop
    return if (name.endsWith("general_store")) {
        GeneralStores.bind(this, name)
    } else {
        inventories.inventory(name)
    }
}

suspend fun TargetInteraction<Player, NPC>.buy(item: String, cost: Int, message: String = "Oh dear. I don't seem to have enough money."): Boolean {
    player.inventory.transaction {
        remove("coins", cost)
        add(item)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.None -> return true
        else -> player<Sad>(message)
    }
    return false
}

suspend fun Player.buy(item: String, cost: Int, message: String = "Oh dear. I don't seem to have enough money."): Boolean {
    inventory.transaction {
        remove("coins", cost)
        add(item)
    }
    when (inventory.transaction.error) {
        is TransactionError.Full -> inventoryFull()
        TransactionError.None -> return true
        else -> player<Sad>(message)
    }
    return false
}
