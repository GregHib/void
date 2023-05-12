import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.contain.clear
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.destroy
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && (option == "Destroy" || option == "Dismiss" || option == "Release") }) { player: Player ->
    if (!item.isNotEmpty() || item.amount <= 0) {
        logger.info { "Error destroying item $item for $player" }
        return@on
    }
    val destroy = destroy("""
        Are you sure you want to ${option.lowercase()} ${item.def.name}?
        You won't be able to reclaim it.
    """, item.id)
    if (!destroy) {
        return@on
    }
    player.inventory.clear(slot)
    when (player.inventory.transaction.error) {
        TransactionError.None -> {
            player.playSound("destroy_object")
            logger.info { "$player destroyed item $item" }
        }
        else -> logger.info { "Error destroying item $item for $player" }
    }
}