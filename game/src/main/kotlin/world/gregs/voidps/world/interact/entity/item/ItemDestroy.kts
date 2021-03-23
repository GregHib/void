import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.type.destroy
import world.gregs.voidps.world.interact.entity.player.equip.ContainerAction

val decoder: ItemDefinitions by inject()
val logger = InlineLogger()

on<ContainerAction>({ container == "inventory" && option == "Destroy" }) { player: Player ->
    val id = player.inventory.getItem(slot)
    val amount = player.inventory.getAmount(slot)
    if(id != -1 && amount > 0) {
        val item = decoder.get(id)
        player.dialogue {
            val destroy = destroy("""
                Are you sure you want to destroy ${item.name}?
                You won't be able to reclaim it.
            """, id)
            if(destroy) {
                if (player.inventory.clear(slot)) {
                    logger.info { "$player destroyed item $id $amount" }
                } else {
                    logger.info { "Error destroying item $id $amount for $player" }
                }
            }
        }
    } else {
        logger.info { "Error destroying item $id $amount for $player" }
    }

}