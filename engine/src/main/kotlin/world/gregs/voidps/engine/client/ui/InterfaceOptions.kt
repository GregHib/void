package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.math.min

class InterfaceOptions(
    private val player: Player,
    private val definitions: InterfaceDefinitions,
    private val inventoryDefinitions: InventoryDefinitions
) {

    fun get(id: String, component: String, index: Int): String {
        return get(id, component).getOrNull(index) ?: ""
    }

    fun get(id: String, component: String): Array<String?> {
        return definitions.getComponent(id, component)?.getOrNull("options") ?: emptyArray()
    }

    fun send(id: String, component: String) {
        val comp = definitions.getComponent(id, component) ?: return
        val script = if (comp["primary", true]) "primary_options" else "secondary_options"
        val inventory = inventoryDefinitions.get(comp["inventory", ""])
        if (inventory.id != -1) {
            val combined = (comp["parent", -1] shl 16) or comp.id
            val all = get(id, component)
            val options = all.copyOfRange(0, min(9, all.size))
            player.sendScript(script, combined, inventory.id, inventory["width", 0], inventory["height", 0], 0, -1, *options)
        }
    }

    fun unlockAll(id: String, component: String, slots: IntRange = -1..-1) {
        val comp = definitions.getComponent(id, component) ?: return
        val options = get(id, component)
        var setting = 0
        for ((index, option) in options.withIndex()) {
            if (option != "") {
                setting += (2 shl index)
            }
        }
        player.sendInterfaceSettings(comp["parent", -1], comp.id, slots.first, slots.last, setting)
    }

    fun unlock(id: String, component: String, slots: IntRange = -1..-1, vararg options: String) {
        unlock(id, component, slots, options.toSet())
    }

    fun unlock(id: String, component: String, slots: IntRange = -1..-1, options: Set<String>) {
        val comp = definitions.getComponent(id, component) ?: return
        val opts = get(id, component)
        var setting = 0
        for ((index, option) in opts.withIndex()) {
            if (options.contains(option)) {
                setting += (2 shl index)
            }
        }
        player.sendInterfaceSettings(comp["parent", -1], comp.id, slots.first, slots.last, setting)
    }

    fun lockAll(id: String, component: String, range: IntRange = -1..-1) {
        val comp = definitions.getComponent(id, component) ?: return
        player.sendInterfaceSettings(comp["parent", -1], comp.id, range.first, range.last, 0)
    }
}