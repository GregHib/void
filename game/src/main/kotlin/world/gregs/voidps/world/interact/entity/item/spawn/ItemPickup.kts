import kotlinx.coroutines.cancel
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject

val items: FloorItems by inject()
val definitions: ItemDefinitions by inject()

on<FloorItemOption>({ option == "Take" }) { player: Player ->
    val item = floorItem
    val id = definitions.getName(floorItem.id)
    item.disappear?.cancel("Floor item picked up.")
    val result = player.inventory.add(id, item.amount)
    if(result) {
        items.remove(item)
    } else {
        when(player.inventory.result) {
            ContainerResult.Full, ContainerResult.Overflow -> {
                player.message("Your inventory is full.")
            }
            else -> println("Failure ${player.inventory.result}")
        }
        // TODO
    }
}