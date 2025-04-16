package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get

data class InterfaceOption(
    override val character: Player,
    val id: String,
    val component: String,
    val optionIndex: Int,
    val option: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction<Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_option"
        1 -> id
        2 -> component
        3 -> option
        4 -> itemSlot.toString()
        else -> null
    }

    companion object {
        val handlers: MutableMap<String, suspend InterfaceOption.() -> Unit> = Object2ObjectOpenHashMap(2_000, Hash.VERY_FAST_LOAD_FACTOR)
    }
}

fun interfaceOption(option: String = "*", component: String = "*", id: String, handler: suspend InterfaceOption.() -> Unit) {
    assert(!id.contains("*")) { "Interface ids cannot contain wildcards. id=$id, component=$component, option='$option'" }
    var added = false
    val definitions = get<InterfaceDefinitions>().get(id)
    if (!option.contains("*")) {
        for (componentDefinition in definitions.components!!.values) {
            if (componentDefinition.stringId != "" && wildcardEquals(component, componentDefinition.stringId)) {
                val key = "$id:${componentDefinition.stringId}:$option"
                InterfaceOption.handlers[key] = handler
                added = true
            }
        }
    } else if (option == "*") {
        for (componentDefinition in definitions.components!!.values) {
            if (componentDefinition.stringId != "" && wildcardEquals(component, componentDefinition.stringId)) {
                val key = "$id:${componentDefinition.stringId}"
                InterfaceOption.handlers[key] = handler
                added = true
            }
        }
    } else {
        for (componentDefinition in definitions.components!!.values) {
            if (componentDefinition.stringId != "" && wildcardEquals(component, componentDefinition.stringId)) {
                var options = componentDefinition.options
                if (check(options, option, id, componentDefinition, handler)) {
                    added = true
                }
                options = componentDefinition.extras?.get("options") as? Array<String?>
                if (check(options, option, id, componentDefinition, handler)) {
                    added = true
                }
            }
        }
    }
    assert(added) { "Unable to find interface id=$id, component=$component, option=$option" }
}

private fun check(options: Array<String?>?, option: String, id: String, componentDefinition: InterfaceComponentDefinition, handler: suspend InterfaceOption.() -> Unit): Boolean {
    var added = false
    if (options != null) {
        for (opt in options) {
            if (opt != null && wildcardEquals(option, opt)) {
                val key = "$id:${componentDefinition.stringId}:$opt"
                InterfaceOption.handlers[key] = handler
                added = true
            }
        }
    }
    return added
}