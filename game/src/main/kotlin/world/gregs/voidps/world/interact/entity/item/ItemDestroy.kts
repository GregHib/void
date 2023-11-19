package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.interact.dialogue.type.destroy
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val logger = InlineLogger()

on<InventoryOption>({ inventory == "inventory" && (option == "Destroy" || option == "Dismiss" || option == "Release") }) { player: Player ->
    if (!item.isNotEmpty() || item.amount <= 0) {
        logger.info { "Error destroying item $item for $player" }
        return@on
    }
    val message = item.def["destroy", """
        Are you sure you want to ${option.lowercase()} ${item.def.name}?
        You won't be able to reclaim it.
    """]
    val destroy = destroy(message, item.id)
    if (!destroy) {
        return@on
    }
    player.inventory.remove(slot, item.id, item.amount)
    when (player.inventory.transaction.error) {
        TransactionError.None -> {
            player.playSound("destroy_object")
            logger.info { "$player destroyed item $item" }
        }
        else -> logger.info { "Error destroying item $item for $player" }
    }
}