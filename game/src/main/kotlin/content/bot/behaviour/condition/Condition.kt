package content.bot.behaviour.condition

import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
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

    companion object {
        internal fun inRange(value: Int, min: Int?, max: Int?): Boolean {
            if (min != null && value < min) {
                return false
            }
            if (max != null && value > max) {
                return false
            }
            return true
        }

        internal fun contains(player: Player, inventory: world.gregs.voidps.engine.inv.Inventory, items: List<BotItem>): Boolean {
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

        private fun parse(type: String, list: List<Map<String, Any>>): Condition? = when (type) {
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

        private fun parseInventory(list: List<Map<String, Any>>): BotInventorySetup = BotInventorySetup(parseItems(list))

        private fun parseItems(list: List<Map<String, Any>>): MutableList<BotItem> {
            val items = mutableListOf<BotItem>()
            for (map in list) {
                val id = map["id"] as? String ?: error("Missing item id in $list")
                var min = map["min"] as? Int
                val max = map["max"] as? Int
                if (min == null && max == null) {
                    min = 1
                }
                val ids = toIds(id)
                items.add(BotItem(ids, min, max))
            }
            return items
        }

        private fun toIds(id: String): Set<String> = id.split(",").flatMap { if (it.any { char -> char == '*' || char == '#' }) Wildcards.get(it, Wildcard.Item) else setOf(it) }.toSet()

        @Suppress("UNCHECKED_CAST")
        private fun parseEquipment(list: List<Map<String, Any>>): BotEquipmentSetup {
            val items = mutableMapOf<EquipSlot, BotItem>()
            for (map in list) {
                for ((key, value) in map) {
                    val slot = EquipSlot.Companion.by(key)
                    require(slot != EquipSlot.None) { "Invalid equipment slot: $key in $list" }
                    value as? Map<String, Any> ?: error("Equipment $key expecting map, found: $value")
                    val id = value["id"] as? String ?: error("Missing item id in $list")
                    var min = value["min"] as? Int
                    val max = value["max"] as? Int
                    if (min == null && max == null) {
                        min = 1
                    }
                    items[slot] = BotItem(
                        ids = toIds(id),
                        min = min,
                        max = max,
                    )
                }
            }
            return BotEquipmentSetup(items)
        }

        private fun parseBank(list: List<Map<String, Any>>): BotBankSetup = BotBankSetup(parseItems(list))

        private fun parseOwns(list: List<Map<String, Any>>): BotOwnsItem? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotOwnsItem(map["id"] as String, min = map["min"] as? Int, max = map["max"] as? Int)
            }
            return null
        }

        @Suppress("UNCHECKED_CAST")
        private fun parseVariable(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("equals")) {
                return BotVariable(
                    id = map["id"] as String,
                    equals = map["equals"]!!,
                    default = map["default"]!!,
                )
            } else if (map.containsKey("min") || map.containsKey("max")) {
                return BotVariableIn(
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
                    return BotClockRemaining(
                        id = map["id"] as String,
                        min = map["min"] as? Int,
                        max = map["max"] as? Int,
                        seconds = map["seconds"] as? Boolean ?: false,
                    )
                }
                return BotHasClock(id = map["id"] as String)
            }
            return null
        }

        private fun parseTimer(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotHasTimer(id = map["id"] as String)
            }
            return null
        }

        private fun parseQueue(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotHasQueue(id = map["id"] as String)
            }
            return null
        }

        private fun parseObject(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotObjectExists(
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
                return BotAtTile(
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
                return BotInArea(id = map["id"] as String)
            }
            return null
        }

        private fun parseCombat(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("min") || map.containsKey("max")) {
                return BotCombatLevel(min = map["min"] as? Int, max = map["max"] as? Int)
            }
            return null
        }

        private fun parseInterface(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotInterfaceOpen(id = map["id"] as String)
            }
            return null
        }

        private fun parseMode(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotHasMode(id = map["id"] as String)
            }
            return null
        }

        private fun parseSkills(list: List<Map<String, Any>>): Condition? {
            val map = list.single()
            if (map.containsKey("id")) {
                return BotSkillLevel(
                    skill = Skill.Companion.of((map["id"] as String).toPascalCase()) ?: error("Unknown skill: '${map["id"]}'"),
                    min = map["min"] as? Int,
                    max = map["max"] as? Int,
                )
            }
            return null
        }
    }
}