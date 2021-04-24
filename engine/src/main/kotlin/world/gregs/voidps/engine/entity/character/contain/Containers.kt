package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItemFactory
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.encode.sendContainerItems
import world.gregs.voidps.utility.get

fun Player.sendContainer(name: String, secondary: Boolean = false) {
    val definitions: ContainerDefinitions = get()
    val container = container(name, definitions.get(name), secondary)
    sendContainer(container)
}

fun Player.sendContainer(container: Container, secondary: Boolean = false) {
    val itemDefs: ItemDefinitions = get()
    sendContainerItems(container.id, container.getItems().map { itemDefs.getId(it) }.toIntArray(), container.getAmounts(), secondary)
}

fun Player.hasContainer(name: String): Boolean {
    return containers.containsKey(name)
}

fun Player.container(definition: ContainerDefinition, secondary: Boolean = false): Container {
    val definitions: ContainerDefinitions = get()
    val name = definitions.getName(definition.id)
    return container(name, definition, secondary)
}

fun Player.container(name: String, secondary: Boolean = false): Container {
    val definitions: ContainerDefinitions = get()
    val container = definitions.get(name)
    return container(name, container, secondary)
}

fun Player.container(name: String, detail: ContainerDefinition, secondary: Boolean = false): Container {
    return containers.getOrPut(if (secondary) "_$name" else name) {
        val itemDefs: ItemDefinitions = get()
        val ids = detail.ids
        val amounts = detail.amounts
        Container(
            items = ids?.map { itemDefs.getName(it) }?.toTypedArray() ?: Array(detail.length) { "" },
            amounts = amounts ?: IntArray(detail.length),
        )
    }.apply {
        if (!setup) {
            id = detail.id
            this.name = if (secondary) "_$name" else name
            capacity = detail.length
            stackMode = detail["stack", StackMode.Normal]
            definitions = get()
            this.events = this@container.events
            this.secondary = secondary
            this.setup = true
        }
    }
}

val Player.inventory: Container
    get() = container("inventory")

val Player.equipment: Container
    get() = container("worn_equipment")

val Player.beastOfBurden: Container
    get() = container("beast_of_burden")

fun Player.purchase(amount: Int, currency: String = "coins"): Boolean {
    if (inventory.remove(currency, amount)) {
        return true
    }
    message("You don't have enough ${currency.replace("_", " ")}.")
    return false
}

fun Player.inventoryFull() = message("You don't have enough inventory space.")

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
                val factory: FloorItemFactory = get()
                factory.spawn(item, overflow, tile, -1, -1, this)
            } else {
                return false
            }
        }
        ContainerResult.Full -> {
            val factory: FloorItemFactory = get()
            factory.spawn(item, amount, tile, -1, -1, this)
        }
    }
    return false
}