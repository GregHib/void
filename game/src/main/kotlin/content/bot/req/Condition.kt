package content.bot.req

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

sealed class Condition {
    abstract fun keys(): Set<String>
    abstract fun events(): Set<String>
    abstract fun check(player: Player): Boolean

    data class Entry(val id: String, val min: Int?, val max: Int?)

    data class Inventory(val items: List<Entry>) : Condition() {
        override fun keys() = items.map { "item:${it.id}" }.toSet()
        override fun events() = setOf("inventory")
        override fun check(player: Player): Boolean {
            for (item in items) {
                val amount = player.inventory.count(item.id)
                if (item.min != null && amount < item.min) {
                    return false
                }
                if (item.max != null && amount > item.max) {
                    return false
                }
            }
            return true
        }

        fun parse(list: List<Map<String, Any>>): Inventory {
            val items = mutableListOf<Entry>()
            for (map in list) {
                val id = map["id"] as String
                var min = map["min"] as? Int
                val max = map["max"] as? Int
                if (min == null && max == null) {
                    min = 0
                }
                items.add(Entry(id, min, max))
            }
            return Inventory(items)
        }
    }

    data class Equipment(val items: Map<EquipSlot, Entry>) : Condition() {
        override fun keys() = items.values.map { "item:${it.id}" }.toSet()
        override fun events() = setOf("worn_equipment")

        override fun check(player: Player): Boolean {
            for ((slot, entry) in items) {
                val item = player.equipped(slot)
                if (item.id != entry.id) {
                    return false
                }
                if (entry.min != null && item.amount < entry.min) {
                    return false
                }
                if (entry.max != null && item.amount > entry.max) {
                    return false
                }
            }
            return true
        }

        @Suppress("UNCHECKED_CAST")
        fun parse(list: List<Map<String, Any>>): Equipment {
            val items = mutableMapOf<EquipSlot, Entry>()
            for (map in list) {
                for ((key, value) in map) {
                    value as Map<String, Any>
                    items[EquipSlot.by(key)] = Entry(
                        id = map["id"] as String,
                        min = map["min"] as? Int ?: 0,
                        max = map["max"] as? Int
                    )
                }
            }
            return Equipment(items)
        }
    }

    data class Variable(val id: String, val equals: Any, val default: Any) : Condition() {
        override fun keys() = setOf("var:$id")
        override fun events() = setOf("variable")
        override fun check(player: Player) = player.variables.get(id, default) == equals

        @Suppress("UNCHECKED_CAST")
        fun parse(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("equals")) {
                return Variable(
                    id = map["id"] as String,
                    equals = map["equals"]!!,
                    default = map["default"]!!,
                )
            } else if (map.containsKey("min")) {
                return VariableIn(
                    id = map["id"] as String,
                    default = map["default"] as Int,
                    min = map["min"] as? Int ?: 0,
                    max = map["max"] as? Int,
                )
            }
            return null
        }
    }

    data class VariableIn(val id: String, val default: Int, val min: Int?, val max: Int?) : Condition() {
        override fun keys() = setOf("var:$id")
        override fun events() = setOf("variable")
        override fun check(player: Player): Boolean {
            val value = player.variables.get(id, default)
            if (min != null && value < min) {
                return false
            }
            if (max != null && value > max) {
                return false
            }
            return true
        }
    }

}