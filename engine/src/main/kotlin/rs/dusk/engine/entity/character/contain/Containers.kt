package rs.dusk.engine.entity.character.contain

import rs.dusk.cache.config.data.ItemContainerDefinition
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemUpdateMessage
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.utility.get

fun Player.sendContainer(name: String, secondary: Boolean = false) {
    val details: ContainerDetails = get()
    val containerId = details.getId(name)
    val container = container(details.get(name), secondary)
    send(ContainerItemsMessage(containerId, container.getItems(), container.getAmounts(), secondary))
}

fun Player.hasContainer(name: String): Boolean {
    val details: ContainerDetails = get()
    val id = details.getId(name)
    return containers.containsKey(id)
}

fun Player.container(name: String, secondary: Boolean = false): Container {
    val details: ContainerDetails = get()
    val container = details.get(name)
    return container(container, secondary)
}

fun Player.container(detail: ItemContainerDefinition, secondary: Boolean = false): Container {
    return containers.getOrPut(if (secondary) -detail.id else detail.id) {
        Container(
            decoder = get(),
            capacity = get<ContainerDetails>().get(detail.id).length,
            listeners = mutableListOf({ updates -> send(ContainerItemUpdateMessage(detail.id, updates.map { Triple(it.index, it.item, it.amount) }, secondary)) }),
            stackMode = detail["stack", StackMode.Normal]
        )
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")