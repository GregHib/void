package rs.dusk.engine.entity.character.contain

import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.contain.detail.ContainerDetail
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemUpdateMessage
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.utility.get

fun Player.sendContainer(name: String, secondary: Boolean = false) {
    val details: ContainerDetails = get()
    val containerId = details.getId(name)
    val container = container(details.get(containerId), secondary)
    send(ContainerItemsMessage(containerId, container.getItems(), container.getAmounts(), secondary))
}

fun Player.hasContainer(name: String): Boolean {
    val details: ContainerDetails = get()
    val id = details.getId(name)
    return containers.containsKey(id)
}

fun Player.container(name: String, secondary: Boolean = false): Container {
    val details: ContainerDetails = get()
    val id = details.getId(name)
    val container = details.get(id)
    return container(container, secondary)
}

fun Player.container(detail: ContainerDetail, secondary: Boolean = false): Container {
    return containers.getOrPut(if(secondary) -detail.id else detail.id) {
        Container(
            decoder = get(),
            capacity = get<ItemContainerDecoder>().get(detail.id).length,
            listeners = mutableListOf({ updates -> send(ContainerItemUpdateMessage(detail.id, updates, secondary)) }),
            stackMode = detail.stack
        )
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.bank: Container
    get() = container("bank")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")