package rs.dusk.engine.client.ui

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

class InterfaceOptions(
    private val player: Player,
    private val details: InterfaceDetails,
    private val containerDetails: ContainerDetails,
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
        val static = details.getComponent(name, component)
        return static.options
    }

    private fun getId(name: String, component: String) = "${name}_$component"

    fun set(name: String, component: String, index: Int, option: String): Boolean {
        val map = options.getOrPut(getId(name, component)) { getStatic(name, component) }
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
        val comp = details.getComponent(name, component)
        val script = if (comp.primaryContainer) 150 else 695
        val id = (comp.parent shl 16) or comp.id
        val options = get(name, component)
        val container = containerDetails.get(comp.container)
        if(container.id != -1) {
            player.send(ScriptMessage(script, id, container.id, container.width, container.height, 0, -1, *options))
        }
    }

    fun unlockAll(name: String, component: String, slots: IntRange = -1..-1) {
        val comp = details.getComponent(name, component)
        val options = get(name, component)
        var setting = 0
        for ((index, option) in options.withIndex()) {
            if (option != "") {
                setting += (2 shl index)
            }
        }
        player.send(InterfaceSettingsMessage(comp.parent, comp.id, slots.first, slots.last, setting))
    }

    fun unlock(name: String, component: String, slots: IntRange = -1..-1, vararg options: String) {
        unlock(name, component, slots, options.toSet())
    }

    fun unlock(name: String, component: String, slots: IntRange = -1..-1, options: Set<String>) {
        val comp = details.getComponent(name, component)
        val opts = get(name, component)
        var setting = 0
        for ((index, option) in opts.withIndex()) {
            if (options.contains(option)) {
                setting += (2 shl index)
            }
        }
        player.send(InterfaceSettingsMessage(comp.parent, comp.id, slots.first, slots.last, setting))
    }

    fun lockAll(name: String, component: String, range: IntRange = -1..-1) {
        val comp = details.getComponent(name, component)
        player.send(InterfaceSettingsMessage(comp.parent, comp.id, range.first, range.last, 0))
    }
}