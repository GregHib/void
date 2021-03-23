import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.FloorItemFactory
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.equip.ContainerAction

val factory: FloorItemFactory by inject()
val logger = InlineLogger()

ContainerAction where { container == "inventory" && option == "Drop" } then {
    val id = player.inventory.getItem(slot)
    val amount = player.inventory.getAmount(slot)
    if (player.inventory.clear(slot) && id != -1 && amount > 0) {
        factory.add(id, amount, player.tile, 60, 60, player)
    } else {
        logger.info { "Error dropping item $id $amount for $player" }
    }
}