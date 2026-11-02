package content.bot.owned

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A set of worn gear and carried items: a preset the owner keeps for all their bots, or what a bot had when it
 * last left the world.
 */
data class Loadout(val worn: Map<EquipSlot, String>, val carried: Map<String, Int>) {
    fun isEmpty(): Boolean = worn.isEmpty() && carried.isEmpty()

    /** "weapon:bronze_sword,hat:iron_full_helm|shark:5,coins:1000" */
    fun encode(): String = worn.entries.joinToString(",") { "${it.key.name.lowercase()}:${it.value}" } + "|" + carried.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun describe(): String = (worn.values + carried.map { (id, amount) -> if (amount > 1) "$amount x $id" else id }).joinToString { it.replace('_', ' ') }

    companion object {
        val EMPTY = Loadout(emptyMap(), emptyMap())

        fun decode(text: String): Loadout {
            val worn = linkedMapOf<EquipSlot, String>()
            val carried = linkedMapOf<String, Int>()
            val wornText = text.substringBefore('|')
            val carriedText = text.substringAfter('|', "")
            for (entry in wornText.split(',')) {
                val slotName = entry.substringBefore(':', "")
                val item = entry.substringAfter(':', "")
                val slot = EquipSlot.entries.firstOrNull { it.name.equals(slotName, ignoreCase = true) } ?: continue
                if (item.isNotBlank()) {
                    worn[slot] = item
                }
            }
            for (entry in carriedText.split(',')) {
                val item = entry.substringBefore(':', "")
                val amount = entry.substringAfter(':', "").toIntOrNull() ?: continue
                if (item.isNotBlank() && amount > 0) {
                    carried[item] = (carried[item] ?: 0) + amount
                }
            }
            return Loadout(worn, carried)
        }

        /** What [equipment] holds by slot and what [inventory] holds by item (counts added up). */
        fun capture(equipment: Inventory, inventory: Inventory?): Loadout {
            val worn = linkedMapOf<EquipSlot, String>()
            for (slot in EquipSlot.entries) {
                if (slot.index < 0) {
                    continue
                }
                val item = equipment.getOrNull(slot.index) ?: continue
                if (item.isNotEmpty()) {
                    worn[slot] = item.id
                }
            }
            val carried = linkedMapOf<String, Int>()
            if (inventory != null) {
                for (item in inventory.items) {
                    if (item.isNotEmpty()) {
                        carried[item.id] = (carried[item.id] ?: 0) + item.amount
                    }
                }
            }
            return Loadout(worn, carried)
        }
    }
}

/**
 * Named loadouts an owner keeps for all their bots, in the persistent owner variable `owned_loadouts`
 * ("name=<loadout>;name2=<loadout>", see [Loadout.encode]).
 */
object OwnedLoadouts {
    const val MAX_PRESETS = 10
    private val NAME = Regex("^(?=.*[a-z])[a-z0-9_]{1,12}$")

    fun all(owner: Player): Map<String, Loadout> {
        val text: String = owner["owned_loadouts", ""]
        if (text.isBlank()) {
            return emptyMap()
        }
        return text.split(';').filter { it.contains('=') }.associate { it.substringBefore('=') to Loadout.decode(it.substringAfter('=')) }
    }

    fun get(owner: Player, name: String): Loadout? = all(owner)[name.lowercase()]

    /** Why [name] can't be a preset name, or null. */
    fun validateName(name: String): String? = if (NAME.matches(name)) null else "A preset name is 1 to 12 letters, numbers or underscores, with at least one letter."

    fun save(owner: Player, name: String, loadout: Loadout): Boolean {
        val presets = all(owner).toMutableMap()
        if (!presets.containsKey(name) && presets.size >= MAX_PRESETS) {
            return false
        }
        presets[name] = loadout
        write(owner, presets)
        return true
    }

    fun delete(owner: Player, name: String): Boolean {
        val presets = all(owner).toMutableMap()
        if (presets.remove(name) == null) {
            return false
        }
        write(owner, presets)
        return true
    }

    private fun write(owner: Player, presets: Map<String, Loadout>) {
        val text = presets.entries.joinToString(";") { "${it.key}=${it.value.encode()}" }
        if (text.isEmpty()) {
            owner.clear("owned_loadouts")
        } else {
            owner["owned_loadouts"] = text
        }
    }
}
