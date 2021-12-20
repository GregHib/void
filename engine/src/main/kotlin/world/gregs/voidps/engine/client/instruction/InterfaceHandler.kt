package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.hasContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.inject

object InterfaceHandler {

    private val itemDefinitions: ItemDefinitions by inject()
    private val interfaceDefinitions: InterfaceDefinitions by inject()
    private val containerDefinitions: ContainerDefinitions by inject()
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
        if (!player.hasContainer(container)) {
            logger.info { "Player doesn't have interface container [$player, interface=$id, container=$container]" }
            return null
        }
        return container
    }

    private fun getContainerItem(player: Player, id: String, componentDefinition: InterfaceComponentDefinition, container: String, item: Int, itemSlot: Int): Item? {
        val itemId = if (item == -1 || item > itemDefinitions.size) "" else itemDefinitions.get(item).stringId
        val slot = when {
            itemSlot == -1 && container == "worn_equipment" -> player.equipment.indexOf(itemId)
            itemSlot == -1 && container == "item_loan" -> 0
            container == "inventory" -> itemSlot
            else -> itemSlot
        }
        val definition = containerDefinitions.get(container)
        if (slot > definition.length || slot < 0) {
            logger.info { "Player interface container out of bounds [$player, container=$container, item index=$itemSlot, container size=${definition.length}]" }
            return null
        }

        val secondary = !componentDefinition["primary", true]
        val container = player.container(definition, secondary = secondary)
        if (!container.isValidId(slot, itemId)) {
            logger.info { "Player invalid interface item [$player, interface=$id, item=$itemId, index=$slot, actual item=${container.getItem(slot)}]" }
            return null
        }
        return container.getItem(slot)
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