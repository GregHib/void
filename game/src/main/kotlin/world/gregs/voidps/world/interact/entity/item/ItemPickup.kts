package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.cancel
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()
val definitions: ItemDefinitions by inject()
val logger = InlineLogger()

on<FloorItemOption>({ option == "Take" }) { player: Player ->
    val item = floorItem
    val id = definitions.getName(floorItem.id)
    item.disappear?.cancel("Floor item picked up.")
    val result = player.inventory.add(id, item.amount)
    if (result) {
        if (items.remove(item)) {
            player.playSound("pickup_item")
        }
    } else {
        when (player.inventory.result) {
            ContainerResult.Full, ContainerResult.Overflow -> player.inventoryFull()
            else -> logger.warn { "Error picking up item $item ${player.inventory.result}" }
        }
    }
}