package rs.dusk.engine.entity.character.contain

import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.contain.detail.ContainerDetail
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.InterfaceItemUpdateMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceItemsMessage
import rs.dusk.utility.get

fun Player.sendContainer(name: String) {
    val details: ContainerDetails = get()
    val containerId = details.getId(name)
    val container = container(details.get(containerId))
    send(InterfaceItemsMessage(containerId, container.items, container.amounts))
}

fun Player.container(name: String): Container {
    val details: ContainerDetails = get()
    val id = details.getId(name)
    val container = details.get(id)
    return container(container)
}

fun Player.container(detail: ContainerDetail): Container {
    return containers.getOrPut(detail.id) {
        Container(
            decoder = get(),
            capacity = get<ItemContainerDecoder>().getSafe(detail.id).length,
            listeners = mutableListOf({ updates -> send(InterfaceItemUpdateMessage(detail.id, updates)) }),
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