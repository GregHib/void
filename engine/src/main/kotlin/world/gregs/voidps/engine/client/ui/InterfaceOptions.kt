package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.network.encode.sendInterfaceSettings
import world.gregs.voidps.network.encode.sendScript
import kotlin.math.min

class InterfaceOptions(
    private val player: Player,
    private val definitions: InterfaceDefinitions,
    private val containerDefinitions: ContainerDefinitions,
    private val options: MutableMap<String, Array<String>> = mutableMapOf()
) {

    fun get(name: String, component: String, index: Int): String {
        return get(name, component).getOrNull(index) ?: ""
    }

    fun get(name: String, component: String): Array<String> {
        val overrides = options[getId(name, component)]
        if (overrides != null) {
            return overrides
        }
        return getStatic(name, component)
    }

    private fun getStatic(name: String, component: String): Array<String> {
        val static = definitions.getComponent(name, component)
        return static.options
    }

    private fun getId(name: String, component: String) = "${name}_$component"

    fun set(name: String, component: String, index: Int, option: String): Boolean {
        val map = options.getOrPut(getId(name, component)) { getStatic(name, component).clone() }
        map[index] = option
        return true
    }

    fun set(name: String, component: String, options: Array<String>): Boolean {
        this.options[getId(name, component)] = options
        return true
    }

    fun remove(name: String, component: String): Boolean {
        return options.remove(getId(name, component)) != null
    }

    fun send(name: String, component: String) {
        val comp = definitions.getComponent(name, component)
        val script = if (comp.primaryContainer) 150 else 695
        val id = (comp.parent shl 16) or comp.id
        val all = get(name, component)
        val options = all.copyOfRange(0, min(9, all.size))
        val container = containerDefinitions.get(comp.container)
        if(container.id != -1) {
            player.sendScript(script, id, container.id, container["width", 0], container["height", 0], 0, -1, *options)
        }
    }

    fun unlockAll(name: String, component: String, slots: IntRange = -1..-1) {
        val comp = definitions.getComponent(name, component)
        val options = get(name, component)
        var setting = 0
        for ((index, option) in options.withIndex()) {
            if (option != "") {
                setting += (2 shl index)
            }
        }
        player.sendInterfaceSettings(comp.parent, comp.id, slots.first, slots.last, setting)
    }

    fun unlock(name: String, component: String, slots: IntRange = -1..-1, vararg options: String) {
        unlock(name, component, slots, options.toSet())
    }

    fun unlock(name: String, component: String, slots: IntRange = -1..-1, options: Set<String>) {
        val comp = definitions.getComponent(name, component)
        val opts = get(name, component)
        var setting = 0
        for ((index, option) in opts.withIndex()) {
            if (options.contains(option)) {
                setting += (2 shl index)
            }
        }
        player.sendInterfaceSettings(comp.parent, comp.id, slots.first, slots.last, setting)
    }

    fun lockAll(name: String, component: String, range: IntRange = -1..-1) {
        val comp = definitions.getComponent(name, component)
        player.sendInterfaceSettings(comp.parent, comp.id, range.first, range.last, 0)
    }
}