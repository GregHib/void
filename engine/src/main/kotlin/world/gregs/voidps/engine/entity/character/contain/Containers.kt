package world.gregs.voidps.engine.entity.character.contain

import com.fasterxml.jackson.annotation.JsonIgnore
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.client.sendContainerItems
import world.gregs.voidps.engine.entity.character.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.entity.character.contain.restrict.ShopRestrictions
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.DependentOnItem
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.utility.get

class Containers(
    val containers: MutableMap<String, ContainerData> = mutableMapOf()
) : MutableMap<String, ContainerData> by containers {

    @JsonIgnore
    val instances: MutableMap<String, Container> = mutableMapOf()

    @JsonIgnore
    lateinit var definitions: ContainerDefinitions

    @JsonIgnore
    lateinit var itemDefinitions: ItemDefinitions

    @JsonIgnore
    lateinit var validItemRule: ItemRestrictionRule

    @JsonIgnore
    lateinit var events: Events

    @JsonIgnore
    lateinit var normalStack: DependentOnItem

    fun container(definition: ContainerDefinition, secondary: Boolean = false): Container {
        return container(definition.stringId, definition, secondary)
    }

    fun container(id: String, secondary: Boolean = false): Container {
        val container = definitions.get(id)
        return container(id, container, secondary)
    }

    fun container(id: String, def: ContainerDefinition, secondary: Boolean = false): Container {
        val shop = def["shop", false]
        val containerId = if (secondary) "_$id" else id
        return instances.getOrPut(containerId) {
            val removalCheck = if (shop) ShopItemRemovalChecker else DefaultItemRemovalChecker
            val data = containers.getOrPut(containerId) {
                val ids = def.ids
                val amounts = def.amounts
                ContainerData(
                    if (ids != null && amounts != null) {
                        Array(def.length) { Item(itemDefinitions.get(ids[it]).stringId, amounts[it]) }
                    } else {
                        Array(def.length) { Item("", removalCheck.getMinimum(it)) }
                    }
                )
            }
            val stackRule = if (shop) AlwaysStack else when (def["stack", "normal"].lowercase()) {
                "always" -> AlwaysStack
                "never" -> NeverStack
                else -> normalStack
            }
            Container(
                data = data,
                id = containerId,
                itemRule = if (shop) ShopRestrictions(data) else validItemRule,
                stackRule = stackRule,
                removalCheck = removalCheck,
            ).apply {
                transaction.changes.bind(events)
            }
        }
    }
}

fun Player.sendContainer(id: String, secondary: Boolean = false) {
    val definitions: ContainerDefinitions = get()
    val container = containers.container(id, definitions.getOrNull(id) ?: return, secondary)
    sendContainer(container)
}

fun Player.sendContainer(container: Container, secondary: Boolean = false) {
    sendContainerItems(
        container = get<ContainerDefinitions>().get(container.id).id,
        items = if (container == inventory || container == equipment) {
            container.items.map { if (it.def.id == -1 && it.amount > 0) 0 else it.def.id }.toIntArray()
        } else {
            container.items.map { it.def.id }.toIntArray()
        },
        amounts = container.items.map { if (it.amount < 0) 0 else it.amount }.toIntArray(),
        primary = secondary
    )
}

val Player.inventory: Container
    get() = containers.container("inventory")

val Player.equipment: Container
    get() = containers.container("worn_equipment")

val Player.beastOfBurden: Container
    get() = containers.container("beast_of_burden")

fun Player.hasItem(id: String) = inventory.contains(id) || equipment.contains(id)

fun Player.hasItem(id: String, amount: Int) = inventory.contains(id, amount) || equipment.contains(id, amount)