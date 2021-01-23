package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.network.codec.game.encode.sendContainerItems
import world.gregs.voidps.network.codec.game.encode.sendInterfaceItemUpdate
import world.gregs.voidps.utility.get

fun Player.sendContainer(name: String, secondary: Boolean = false) {
    val definitions: ContainerDefinitions = get()
    val containerId = definitions.getId(name)
    val container = container(definitions.get(name), secondary)
    sendContainerItems(containerId, container.getItems(), container.getAmounts(), secondary)
}

fun Player.hasContainer(name: String): Boolean {
    val definitions: ContainerDefinitions = get()
    val id = definitions.getId(name)
    return containers.containsKey(id)
}

fun Player.container(name: String, secondary: Boolean = false): Container {
    val definitions: ContainerDefinitions = get()
    val container = definitions.get(name)
    return container(container, secondary)
}

fun Player.container(detail: ContainerDefinition, secondary: Boolean = false): Container {
    return containers.getOrPut(if (secondary) -detail.id else detail.id) {
        Container(
            id = detail.id,
            capacity = get<ContainerDefinitions>().get(detail.id).length,
            listeners = mutableListOf({ updates -> sendInterfaceItemUpdate(detail.id, updates.map { Triple(it.index, it.item, it.amount) }, secondary) }),
            stackMode = detail["stack", StackMode.Normal]
        ).apply {
            definitions = get()
        }
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")