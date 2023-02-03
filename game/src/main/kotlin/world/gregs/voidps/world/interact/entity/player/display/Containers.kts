import world.gregs.voidps.engine.client.sendInterfaceItemUpdate
import world.gregs.voidps.engine.entity.character.contain.ContainerUpdate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

val containerDefs: ContainerDefinitions by inject()
val itemDefs: ItemDefinitions by inject()

on<ContainerUpdate> { player: Player ->
    val secondary = container.startsWith("_")
    val id = if (secondary) container.removePrefix("_") else container
    player.sendInterfaceItemUpdate(
        key = containerDefs.get(id).id,
        updates = updates.map { Triple(it.index, itemDefs.getOrNull(it.item.id)?.id ?: -1, it.item.amount) },
        secondary = secondary
    )
}
