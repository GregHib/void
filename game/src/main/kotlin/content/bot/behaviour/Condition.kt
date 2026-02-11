package content.bot.behaviour

import content.entity.player.bank.bank
import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.InteractOption
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.InterfaceOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.NPCOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.collections.iterator

/**
 * Checks if the world state is as expected or not
 * Sorted by [priority] (lowest first) to avoid resolvers being executed in a weird order
 * E.g. going to an area before getting the equipment.
 */
sealed class Condition(val priority: Int) {
    abstract fun keys(): Set<String>
    abstract fun events(): Set<String>
    abstract fun check(player: Player): Boolean

    data class InArea(val id: String) : Condition(1000) {
        override fun keys() = setOf("area:$id")
        override fun events() = setOf("area")
        override fun check(player: Player) = player.tile in Areas[id]
    }

    data class AtTile(val x: Int? = null, val y: Int? = null, val level: Int? = null) : Condition(1000) {
        override fun keys() = setOf("tile")
        override fun events() = setOf("tile")
        override fun check(player: Player): Boolean {
            if (x != null && player.tile.x != x) {
                return false
            }
            if (y != null && player.tile.y != y) {
                return false
            }
            if (level != null && player.tile.level != level) {
                return false
            }
            return true
        }
    }

    data class Queue(val id: String) : Condition(1) {
        override fun keys() = setOf("queue:$id")
        override fun events() = setOf("queue")
        override fun check(player: Player) = player.queue.contains(id)
    }

    data class Timer(val id: String) : Condition(1) {
        override fun keys() = setOf("timer:$id")
        override fun events() = setOf("timer")
        override fun check(player: Player) = player.timers.contains(id) || player.softTimers.contains(id)
    }

    data class Clock(val id: String) : Condition(1) {
        override fun keys() = setOf("clock:$id")
        override fun events() = setOf("clock")
        override fun check(player: Player) = player.hasClock(id)
    }

    data class ClockRemaining(val id: String, val min: Int? = null, val max: Int? = null, val seconds: Boolean = false) : Condition(1) {
        override fun keys() = setOf("clock:$id")
        override fun events() = setOf("clock")
        override fun check(player: Player): Boolean {
            val remaining = player.remaining(id, if (seconds) epochSeconds() else GameLoop.tick)
            return inRange(remaining, min, max)
        }
    }

    data class Entry(val ids: Set<String>, val min: Int? = null, val max: Int? = null, val usable: Boolean = false, val equippable: Boolean = false)

    data class Inventory(val items: List<Entry>) : Condition(100) {
        override fun keys() = items.flatMap { entry -> entry.ids.map { "item:$it" } }.toSet()
        override fun events() = setOf("inventory")
        override fun check(player: Player) = contains(player, player.inventory, items)
    }

    data class Equipment(val items: Map<EquipSlot, Entry>) : Condition(90) {
        override fun keys() = items.values.flatMap { entry -> entry.ids.map { "item:$it" } }.toSet()
        override fun events() = setOf("worn_equipment")

        override fun check(player: Player): Boolean {
            for ((slot, entry) in items) {
                val item = player.equipped(slot)
                if (entry.ids.contains("empty")) {
                    if (item.isEmpty()) {
                        continue
                    }
                    return false
                }
                if (!entry.ids.contains(item.id)) {
                    return false
                }
                if (!inRange(item.amount, entry.min, entry.max)) {
                    return false
                }
                if (entry.usable && item.amount > 0 && !player.hasRequirementsToUse(item)) {
                    return false
                }
            }
            return true
        }
    }

    data class Bank(val items: List<Entry>) : Condition(80) {
        override fun keys() = items.flatMap { entry -> entry.ids.map { "bank:$it" } }.toSet()
        override fun events() = setOf("bank")
        override fun check(player: Player) = contains(player, player.bank, items)
    }

    data class Owns(val id: String, val min: Int? = null, val max: Int? = null) : Condition(110) {
        override fun keys() = setOf("item:$id")
        override fun events() = setOf("worn_equipment", "inventory", "bank")
        override fun check(player: Player): Boolean {
            val count = player.inventory.count(id) + player.bank.count(id) + player.equipment.count(id)
            return inRange(count, min, max)
        }
    }

    data class Variable(val id: String, val equals: Any, val default: Any) : Condition(1) {
        override fun keys() = setOf("var:$id")
        override fun events() = setOf("variable")
        override fun check(player: Player) = player.variables.get(id, default) == equals
    }

    data class VariableIn(val id: String, val default: Int, val min: Int?, val max: Int?) : Condition(1) {
        override fun keys() = setOf("var:$id")
        override fun events() = setOf("variable")
        override fun check(player: Player): Boolean {
            val value = player.variables.get(id, default)
            return inRange(value, min, max)
        }
    }

    data class ObjectExists(val id: String, val x: Int, val y: Int) : Condition(900) {
        override fun keys() = setOf("obj:$id")
        override fun events() = setOf("object")
        override fun check(player: Player) = GameObjects.findOrNull(player.tile.copy(x, y), id) != null
    }

    data class CombatLevel(val min: Int? = null, val max: Int? = null) : Condition(1) {
        override fun keys() = setOf("skill:combat")
        override fun events() = setOf("skill")
        override fun check(player: Player) = inRange(player.combatLevel, min, max)
    }

    data class InterfaceOpen(val id: String) : Condition(1) {
        override fun keys() = setOf("iface:$id")
        override fun events() = setOf("interface")
        override fun check(player: Player) = player.interfaces.contains(id)
    }

    data class SkillLevel(val skill: Skill, val min: Int? = null, val max: Int? = null) : Condition(1) {
        override fun keys() = setOf("skill:${skill.name}")
        override fun events() = setOf("skill:${skill.name}")
        override fun check(player: Player) = inRange(player.levels.get(skill), min, max)
    }

    data class HasMode(val id: String) : Condition(1) {
        override fun keys() = setOf("mode:${id}")
        override fun events() = setOf("mode")
        override fun check(player: Player) = when (id) {
            "empty" -> player.mode == EmptyMode
            "interact" -> player.mode is Interact
            "interact_on" -> player.mode is InteractOption
            "combat_movement" -> player.mode is CombatMovement
            "interface_on_floor_item" -> player.mode is InterfaceOnFloorItemInteract
            "interface_on_npc" -> player.mode is InterfaceOnNPCInteract
            "interface_on_object" -> player.mode is InterfaceOnObjectInteract
            "item_on_floor_item" -> player.mode is ItemOnFloorItemInteract
            "item_on_npc" -> player.mode is ItemOnNPCInteract
            "item_on_object" -> player.mode is ItemOnObjectInteract
            "item_on_player" -> player.mode is ItemOnPlayerInteract
            "npc_on_floor_item" -> player.mode is NPCOnFloorItemInteract
            "npc_on_npc" -> player.mode is NPCOnNPCInteract
            "npc_on_object" -> player.mode is NPCOnObjectInteract
            "npc_on_player" -> player.mode is NPCOnPlayerInteract
            "player_on_floor_item" -> player.mode is PlayerOnFloorItemInteract
            "player_on_npc" -> player.mode is PlayerOnNPCInteract
            "player_on_object" -> player.mode is PlayerOnObjectInteract
            "player_on_player" -> player.mode is PlayerOnPlayerInteract
            "movement" -> player.mode is Movement
            "follow" -> player.mode is Follow
            "face" -> player.mode is Face
            "patrol" -> player.mode is Patrol
            "pause" -> player.mode == PauseMode
            "rest" -> player.mode is Rest
            "retreat" -> player.mode is Retreat
            "wander" -> player.mode is Wander
            else -> false
        }
    }

    companion object {
        private fun inRange(value: Int, min: Int?, max: Int?): Boolean {
            if (min != null && value < min) {
                return false
            }
            if (max != null && value > max) {
                return false
            }
            return true
        }

        private fun contains(player: Player, inventory: world.gregs.voidps.engine.inv.Inventory, items: List<Entry>): Boolean {
            for (item in items) {
                var found = false
                for (id in item.ids) {
                    val amount = if (id == "empty") {
                        inventory.spaces
                    } else {
                        inventory.count(id)
                    }
                    if (!inRange(amount, item.min, item.max)) {
                        continue
                    }
                    if (item.usable && amount > 0 && id != "empty") {
                        val index = inventory.indexOf(id)
                        if (index == -1) {
                            error("Unable to find item $id in inventory.")
                        }
                        val item = inventory[index]
                        if (!player.hasRequirementsToUse(item)) {
                            continue
                        }
                    }
                    if (item.equippable && amount > 0 && id != "empty") {
                        val index = inventory.indexOf(id)
                        if (index == -1) {
                            error("Unable to find item $id in inventory.")
                        }
                        val item = inventory[index]
                        if (!player.hasRequirements(item)) {
                            continue
                        }
                    }
                    found = true
                }
                if (!found) {
                    return false
                }
            }
            return true
        }

        fun parse(list: List<Pair<String, List<Map<String, Any>>>>, name: String): List<Condition> {
            val requirements = mutableListOf<Condition>()
            for ((type, value) in list) {
                val condition = parse(type, value) ?: error("No condition parser for '$type' in $name.")
                requirements.add(condition)
            }
            requirements.sortBy { it.priority }
            return requirements
        }

        fun parse(type: String, list: List<Map<String, Any>>): Condition? = when (type) {
            "inventory" -> parseInventory(list)
            "equipment" -> parseEquipment(list)
            "bank" -> parseBank(list)
            "owns" -> parseOwns(list)
            "variable" -> parseVariable(list)
            "clock" -> parseClock(list)
            "timer" -> parseTimer(list)
            "queue" -> parseQueue(list)
            "area" -> parseArea(list)
            "tile" -> parseTile(list)
            "object" -> parseObject(list)
            "combat_level" -> parseCombat(list)
            "interface_open" -> parseInterface(list)
            "mode" -> parseMode(list)
            "skill" -> parseSkills(list)
            else -> null
        }

        private fun parseInventory(list: List<Map<String, Any>>): Inventory = Inventory(parseItems(list))

        private fun parseItems(list: List<Map<String, Any>>): MutableList<Entry> {
            val items = mutableListOf<Entry>()
            for (map in list) {
                val id = map["id"] as? String ?: error("Missing item id in $list")
                var min = map["min"] as? Int
                val max = map["max"] as? Int
                if (min == null && max == null) {
                    min = 1
                }
                val ids = toIds(id)
                items.add(Entry(ids, min, max))
            }
            return items
        }

        private fun toIds(id: String): Set<String> = id.split(",").flatMap { if (it.any { char -> char == '*' || char == '#' }) Wildcards.get(it, Wildcard.Item) else setOf(it) }.toSet()

        @Suppress("UNCHECKED_CAST")
        private fun parseEquipment(list: List<Map<String, Any>>): Equipment {
            val items = mutableMapOf<EquipSlot, Entry>()
            for (map in list) {
                for ((key, value) in map) {
                    val slot = EquipSlot.by(key)
                    require(slot != EquipSlot.None) { "Invalid equipment slot: $key in $list" }
                    value as? Map<String, Any> ?: error("Equipment $key expecting map, found: $value")
                    val id = value["id"] as? String ?: error("Missing item id in $list")
                    var min = value["min"] as? Int
                    val max = value["max"] as? Int
                    if (min == null && max == null) {
                        min = 1
                    }
                    items[slot] = Entry(
                        ids = toIds(id),
                        min = min,
                        max = max,
                    )
                }
            }
            return Equipment(items)
        }

        private fun parseBank(list: List<Map<String, Any>>): Bank = Bank(parseItems(list))

        private fun parseOwns(list: List<Map<String, Any>>): Owns? {
            val map = list.single()
            if (map.containsKey("id")) {
                return Owns(map["id"] as String, min = map["min"] as? Int, max = map["max"] as? Int)
            }
            return null
        }

        @Suppress("UNCHECKED_CAST")
        private fun parseVariable(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("equals")) {
                return Variable(
                    id = map["id"] as String,
                    equals = map["equals"]!!,
                    default = map["default"]!!,
                )
            } else if (map.containsKey("min") || map.containsKey("max")) {
                return VariableIn(
                    id = map["id"] as String,
                    default = map["default"] as Int,
                    min = map["min"] as? Int ?: 0,
                    max = map["max"] as? Int,
                )
            }
            return null
        }

        private fun parseClock(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                if (map.containsKey("min") || map.containsKey("max")) {
                    return ClockRemaining(
                        id = map["id"] as String,
                        min = map["min"] as? Int,
                        max = map["max"] as? Int,
                        seconds = map["seconds"] as? Boolean ?: false,
                    )
                }
                return Clock(id = map["id"] as String)
            }
            return null
        }

        private fun parseTimer(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return Timer(id = map["id"] as String)
            }
            return null
        }

        private fun parseQueue(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return Queue(id = map["id"] as String)
            }
            return null
        }

        private fun parseObject(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return ObjectExists(
                    id = map["id"] as String,
                    x = map["x"] as Int,
                    y = map["y"] as Int,
                )
            }
            return null
        }

        private fun parseTile(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("x") || map.containsKey("y") || map.containsKey("level")) {
                return AtTile(
                    x = map["x"] as? Int,
                    y = map["y"] as? Int,
                    level = map["level"] as? Int,
                )
            }
            return null
        }

        private fun parseArea(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return InArea(id = map["id"] as String)
            }
            return null
        }

        private fun parseCombat(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("min") || map.containsKey("max")) {
                return CombatLevel(min = map["min"] as? Int, max = map["max"] as? Int)
            }
            return null
        }

        private fun parseInterface(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return InterfaceOpen(id = map["id"] as String)
            }
            return null
        }

        private fun parseMode(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return HasMode(id = map["id"] as String)
            }
            return null
        }

        private fun parseSkills(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return SkillLevel(
                    skill = Skill.of((map["id"] as String).toPascalCase()) ?: error("Unknown skill: '${map["id"]}'"),
                    min = map["min"] as? Int,
                    max = map["max"] as? Int,
                )
            }
            return null
        }
    }
}
