package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && option == "Drop" }) { player: Player ->
    if (player.inventory.clear(slot) && item.isNotEmpty() && item.amount > 0) {
        items.add(item.name, item.amount, player.tile, 60, 60, player)
        player.playSound("drop_item")
    } else {
        logger.info { "Error dropping item $item for $player" }
    }
}