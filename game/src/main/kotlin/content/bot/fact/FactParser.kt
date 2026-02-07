package content.bot.fact

import world.gregs.voidps.type.Tile

sealed class FactParser<T> {
    open val required: Set<String> = emptySet()
    abstract fun parse(map: Map<String, Any>): Fact<T>
    abstract fun predicate(map: Map<String, Any>): Predicate<T>?

    fun requirement(map: Map<String, Any>) = Requirement(parse(map), predicate(map))

    fun check(map: Map<String, Any>): String? {
        for (key in required) {
            if (!map.containsKey(key)) {
                return "missing key '$key' in map ${map}"
            }
        }
        return null
    }

    object InventorySpace : FactParser<Int>() {
        override fun parse(map: Map<String, Any>) = Fact.InventorySpace
        override fun predicate(map: Map<String, Any>) = Predicate.parseInt(map)
    }

    object InventoryCount : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.InventoryCount(map["id"] as String)
        override fun predicate(map: Map<String, Any>): Predicate<Int>? {
            if (!map.containsKey("min") && !map.containsKey("equals")) {
                map as MutableMap<String, Any>
                map["min"] = 1
            }
            return Predicate.parseInt(map)
        }
    }

    object ItemCount : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.ItemCount(map["id"] as String)
        override fun predicate(map: Map<String, Any>): Predicate<Int>? {
            if (!map.containsKey("min") && !map.containsKey("equals")) {
                map as MutableMap<String, Any>
                map["min"] = 1
            }
            return Predicate.parseInt(map)
        }
    }

    object BankCount : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.BankCount(map["id"] as String)
        override fun predicate(map: Map<String, Any>) = Predicate.parseInt(map)
    }

    object EquipCount : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.EquipCount(map["id"] as String)
        override fun predicate(map: Map<String, Any>): Predicate<Int>? {
            if (!map.containsKey("min") && !map.containsKey("equals")) {
                map as MutableMap<String, Any>
                map["min"] = 1
            }
            return Predicate.parseInt(map)
        }
    }

    object Variable : FactParser<Any>() {
        override val required = setOf("id", "default")
        override fun parse(map: Map<String, Any>): Fact<Any> {
            val id = map["id"] as String
            return when (val default = map["default"]) {
                is Int -> Fact.IntVariable(id, default)
                is String -> Fact.StringVariable(id, default)
                is Double -> Fact.DoubleVariable(id, default)
                is Boolean -> Fact.BoolVariable(id, default)
                else -> error("Invalid default value $default")
            } as Fact<Any>
        }
        override fun predicate(map: Map<String, Any>): Predicate<Any> {
            return when (val default = map["default"]) {
                is Int -> Predicate.parseInt(map)
                is String -> Predicate.parseString(map)
                is Double -> Predicate.parseDouble(map)
                is Boolean -> Predicate.parseBool(map)
                else -> error("Invalid default value $default")
            } as Predicate<Any>
        }
    }

    object Clock : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>): Fact<Int> {
            return Fact.ClockRemaining(map["id"] as String, map["seconds"] as? Boolean ?: false)
        }
        override fun predicate(map: Map<String, Any>) = Predicate.parseInt(map)
    }

    object Timer : FactParser<Boolean>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.HasTimer(map["id"] as String)
        override fun predicate(map: Map<String, Any>) = Predicate.BooleanTrue
    }

    object Queue : FactParser<Boolean>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.HasQueue(map["id"] as String)
        override fun predicate(map: Map<String, Any>) = Predicate.BooleanTrue
    }

    object Interface : FactParser<Boolean>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.InterfaceOpen(map["id"] as String)
        override fun predicate(map: Map<String, Any>) = Predicate.BooleanTrue
    }

    object PlayerTile : FactParser<Tile>() {
        override fun parse(map: Map<String, Any>) = Fact.PlayerTile
        override fun predicate(map: Map<String, Any>) = Predicate.parseTile(map)
    }

    object CombatLevel : FactParser<Int>() {
        override fun parse(map: Map<String, Any>) = Fact.CombatLevel
        override fun predicate(map: Map<String, Any>) = Predicate.parseInt(map)
    }

    object Skill : FactParser<Int>() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>) = Fact.SkillLevel.of(map["id"] as String)
        override fun predicate(map: Map<String, Any>) = Predicate.parseInt(map)
    }

    companion object {
        val parsers = mapOf(
            "inventory_space" to InventorySpace,
            "carries" to InventoryCount,
            "owns" to ItemCount,
            "banked" to BankCount,
            "equips" to EquipCount,
            "variable" to Variable,
            "clock" to Clock,
            "has_timer" to Timer,
            "interface_open" to Interface,
            "has_queue" to Queue,
            "tile" to PlayerTile,
            "area" to PlayerTile,
            "combat_level" to CombatLevel,
            "skill" to Skill,
        )
    }
}
