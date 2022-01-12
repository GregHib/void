package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendContainerItems
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.toTitleCase

fun Player.sendContainer(id: String, secondary: Boolean = false) {
    val definitions: ContainerDefinitions = get()
    val container = container(id, definitions.get(id), secondary)
    sendContainer(container)
}

fun Player.sendContainer(container: Container, secondary: Boolean = false) {
    sendContainerItems(
        container = get<ContainerDefinitions>().get(container.id).id,
        items = if (container == inventory || container == equipment) {
            container.getItems().map { if (it.def.id == -1 && it.amount > 0) 0 else it.def.id }.toIntArray()
        } else {
            container.getItems().map { it.def.id }.toIntArray()
        },
        amounts = container.getItems().map { if (it.amount < 0) 0 else it.amount }.toIntArray(),
        primary = secondary
    )
}

fun Player.hasContainer(id: String): Boolean {
    return containers.containsKey(id)
}

fun Player.container(definition: ContainerDefinition, secondary: Boolean = false): Container {
    return container(definition.stringId, definition, secondary)
}

fun Player.container(id: String, secondary: Boolean = false): Container {
    val definitions: ContainerDefinitions = get()
    val container = definitions.get(id)
    return container(id, container, secondary)
}

fun Player.container(id: String, def: ContainerDefinition, secondary: Boolean = false): Container {
    val shop = def["shop", false]
    return containers.getOrPut(if (secondary) "_$id" else id) {
        Container(items = Array(def.length) { Item("", if (shop) -1 else 0) })
    }.apply {
        Container.setup(
            container = this,
            capacity = def.length,
            secondary = secondary,
            id = if (secondary) "_$id" else id,
            minimumAmount = if (shop) -1 else 0,
            stackMode = if (shop) StackMode.Always else def["stack", StackMode.Normal],
            events = this@container.events
        )
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")

fun Player.hasItem(item: String) = inventory.contains(item) || equipment.contains(item)

fun Player.hasItem(item: String, amount: Int) = inventory.contains(item, amount) || equipment.contains(item, amount)

fun Player.purchase(amount: Int, currency: String = "coins"): Boolean {
    if (inventory.remove(currency, amount)) {
        return true
    }
    message("You don't have enough ${currency.toTitleCase()}.")
    return false
}

/**
 * Adds [item] to [inventory] and drops excess
 */
fun Player.give(item: String, amount: Int): Boolean {
    inventory.add(item, amount)
    when (inventory.result) {
        ContainerResult.Success -> return true
        ContainerResult.Overflow -> {
            val index = inventory.indexOf(item)
            val current = inventory.getAmount(index)
            val overflow = ((current.toLong() + amount) - Int.MAX_VALUE).toInt()
            val fill = Int.MAX_VALUE - current
            if ((fill == 0 || inventory.add(item, fill)) && overflow > 0) {
                val items: FloorItems = get()
                items.add(item, overflow, tile, -1, -1, this)
            } else {
                return false
            }
        }
        ContainerResult.Full -> {
            val items: FloorItems = get()
            items.add(item, amount, tile, -1, -1, this)
        }
    }
    return false
}