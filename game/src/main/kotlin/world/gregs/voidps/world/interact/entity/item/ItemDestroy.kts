import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.destroy
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && (option == "Destroy" || option == "Dismiss" || option == "Release") }) { player: Player ->
    if(item.isNotEmpty() && item.amount > 0) {
        player.dialogue {
            val destroy = destroy("""
                Are you sure you want to ${option.lowercase()} ${item.def.name}?
                You won't be able to reclaim it.
            """, item.id)
            if(destroy) {
                if (player.inventory.clear(slot)) {
                    logger.info { "$player destroyed item $item" }
                } else {
                    logger.info { "Error destroying item $item for $player" }
                }
            }
        }
    } else {
        logger.info { "Error destroying item $item for $player" }
    }
}