package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.data.definition.extra.ContainerDefinitions
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class InterfaceHandler(
    private val itemDefinitions: ItemDefinitions,
    private val interfaceDefinitions: InterfaceDefinitions,
    private val containerDefinitions: ContainerDefinitions
) {
    private val logger = InlineLogger()

    fun getInterfaceItem(player: Player, interfaceId: Int, componentId: Int, itemId: Int, itemSlot: Int): InterfaceData? {
        val id = getOpenInterface(player, interfaceId) ?: return null
        val componentDefinition = getContainerDefinition(player, interfaceId, componentId) ?: return null
        val component = componentDefinition.stringId
        var item = Item.EMPTY
        var container = ""
        if (itemId != -1) {
            container = getContainer(player, id, component, componentDefinition) ?: return null
            item = getContainerItem(player, id, componentDefinition, container, itemId, itemSlot) ?: return null
        }
        return InterfaceData(id, component, item, container, componentDefinition.options)
    }

    private fun getOpenInterface(player: Player, interfaceId: Int): String? {
        val id = interfaceDefinitions.get(interfaceId).stringId
        if (!player.interfaces.contains(id)) {
            logger.info { "Player doesn't have interface open [$player, interface=$id]" }
            return null
        }
        return id
    }

    private fun getContainerDefinition(player: Player, id: Int, componentId: Int): InterfaceComponentDefinition? {
        val interfaceDefinition = interfaceDefinitions.get(id)
        val componentDefinition = interfaceDefinition.components?.get(componentId)
        if (componentDefinition == null) {
            logger.info { "Interface doesn't have component [$player, interface=$id, component=$componentId]" }
            return null
        }
        return componentDefinition
    }

    private fun getContainer(player: Player, id: String, component: String, componentDefinition: InterfaceComponentDefinition): String? {
        if (component.isEmpty()) {
            logger.info { "No container component found [$player, interface=$id, component=$component]" }
            return null
        }
        val container = componentDefinition["container", ""]
        if (!player.containers.containsKey(container)) {
            logger.info { "Player doesn't have interface container [$player, interface=$id, container=$container]" }
            return null
        }
        return container
    }

    private fun getContainerItem(player: Player, id: String, componentDefinition: InterfaceComponentDefinition, containerId: String, item: Int, itemSlot: Int): Item? {
        val itemId = if (item == -1 || item > itemDefinitions.size) "" else itemDefinitions.get(item).stringId
        val slot = when {
            itemSlot == -1 && containerId == "worn_equipment" -> player.equipment.indexOf(itemId)
            itemSlot == -1 && containerId == "item_loan" -> 0
            containerId == "inventory" -> itemSlot
            else -> itemSlot
        }
        val definition = containerDefinitions.get(containerId)
        if (slot > definition.length || slot < 0) {
            logger.info { "Player interface container out of bounds [$player, container=$containerId, item_index=$itemSlot, container_size=${definition.length}]" }
            return null
        }

        val secondary = !componentDefinition["primary", true]
        val container = player.containers.container(definition, secondary = secondary)
        if (!container.inBounds(slot) || container[slot].id != itemId) {
            logger.info { "Player invalid interface item [$player, interface=$id, index=$slot, expected_item=$itemId, actual_item=${container[slot]}]" }
            return null
        }
        return container[slot]
    }
}

data class InterfaceData(
    val id: String,
    val component: String,
    val item: Item,
    val container: String,
    val options: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterfaceData

        if (id != other.id) return false
        if (component != other.component) return false
        if (item != other.item) return false
        if (container != other.container) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + component.hashCode()
        result = 31 * result + item.hashCode()
        result = 31 * result + container.hashCode()
        result = 31 * result + (options?.contentHashCode() ?: 0)
        return result
    }
}