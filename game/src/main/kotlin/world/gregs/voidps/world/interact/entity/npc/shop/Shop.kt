package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.type.player

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

suspend fun TargetNPCContext<Player>.buy(item: String, cost: Int, message: String = "Oh dear. I don't seem to have enough money."): Boolean {
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