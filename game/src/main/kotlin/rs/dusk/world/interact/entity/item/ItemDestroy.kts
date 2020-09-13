import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.dialogue.dialogue
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.dialogue.type.destroy
import rs.dusk.world.interact.entity.player.equip.ContainerAction

val decoder: ItemDecoder by inject()
val logger = InlineLogger()

ContainerAction where { container == "inventory" && option == "Destroy" } then {
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