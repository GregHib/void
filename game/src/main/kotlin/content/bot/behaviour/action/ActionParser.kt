package content.bot.behaviour.action

import content.bot.behaviour.condition.Condition
import content.bot.behaviour.utility.UtilityCurveParser
import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed class ActionParser {
    open val required = emptySet<String>()
    open val optional = emptySet<String>()

    abstract fun parse(map: Map<String, Any>): BotAction

    fun check(map: Map<String, Any>): String? {
        for (key in required) {
            if (!map.containsKey(key)) {
                return "missing key '$key' in map $map"
            }
        }
        for (key in map.keys) {
            if (!map.containsKey(key) && !optional.contains(key)) {
                return "unexpected key '$key' in map $map"
            }
        }
        return null
    }

    object InteractNpcParser : ActionParser() {
        override val required = setOf("id", "option")
        override val optional = setOf("delay", "success", "radius", "heal_percent", "loot_over_value")

        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as String
            val id = map["id"] as String
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            val radius = map["radius"] as? Int ?: 10
            return if (option == "Attack") {
                val healPercent = map["heal_percent"] as? Int ?: 20
                val lootOverValue = map["loot_over_value"] as? Int ?: 0
                BotFightNpc(id, delay, success, radius, healPercent, lootOverValue)
            } else {
                BotInteractNpc(option, id, delay, success, radius)
            }
        }
    }

    object InteractPlayerParser : ActionParser() {
        override val required = setOf("option")
        override val optional = setOf("delay", "success", "radius", "heal_percent", "loot_over_value", "loot_strategy", "target_score", "area")

        @Suppress("UNCHECKED_CAST")
        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as String
            require(option == "Attack") { "Only 'Attack' option is supported for 'player' actions, got '$option'." }
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            val radius = map["radius"] as? Int ?: 10
            val healPercent = map["heal_percent"] as? Int ?: 20
            val lootOverValue = map["loot_over_value"] as? Int ?: 0
            val lootStrategy = BotLootStrategy.of(map["loot_strategy"] as? String)
            val rawScore = map["target_score"] as? List<Map<String, Any>>
            val scorer = rawScore?.let { UtilityCurveParser.parseScorer(it) }
            val area = map["area"] as? String
            return BotFightPlayer(delay, success, radius, healPercent, lootOverValue, lootStrategy, scorer, area)
        }
    }

    object PrayParser : ActionParser() {
        override val required = setOf("id")
        override val optional = setOf("if")

        override fun parse(map: Map<String, Any>): BotAction {
            val id = map["id"] as String
            val condition = requirement(map, "if").singleOrNull()
            return BotPray(id, condition)
        }
    }

    object SpecAttackParser : ActionParser() {
        override val required = setOf("weapon", "fallback")
        override val optional = setOf("min_energy", "if")

        override fun parse(map: Map<String, Any>): BotAction {
            val weapon = map["weapon"] as String
            val fallback = map["fallback"] as String
            val minEnergy = map["min_energy"] as? Int ?: 250
            val condition = requirement(map, "if").singleOrNull()
            return BotSpecAttack(weapon, fallback, minEnergy, condition)
        }
    }

    object DrinkPotionParser : ActionParser() {
        override val required = setOf("item", "skill")
        override val optional = setOf("if")

        override fun parse(map: Map<String, Any>): BotAction {
            val item = map["item"] as String
            val skillName = map["skill"] as String
            val skill = Skill.of(skillName.toPascalCase())
                ?: error("Unknown skill '$skillName' in drink_potion action.")
            val condition = requirement(map, "if").singleOrNull()
            return BotDrinkPotion(item, skill, condition)
        }
    }

    object CastVengeanceParser : ActionParser() {
        override fun parse(map: Map<String, Any>): BotAction = BotCastVengeance
    }

    object CastSpellParser : ActionParser() {
        override val optional = setOf("delay", "success", "radius", "heal_percent", "target_score", "family", "kite", "area")

        @Suppress("UNCHECKED_CAST")
        override fun parse(map: Map<String, Any>): BotAction {
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            val radius = map["radius"] as? Int ?: 10
            val healPercent = map["heal_percent"] as? Int ?: 40
            val rawScore = map["target_score"] as? List<Map<String, Any>>
            val scorer = rawScore?.let { UtilityCurveParser.parseScorer(it) }
            val family = map["family"] as? String ?: "ice"
            val kite = map["kite"] as? Boolean ?: true
            val area = map["area"] as? String
            return BotCastSpell(delay, success, radius, healPercent, scorer, family, kite, area)
        }
    }

    object SwitchSetupParser : ActionParser() {
        override val required = setOf("equipment")
        override val optional = setOf("if")

        @Suppress("UNCHECKED_CAST")
        override fun parse(map: Map<String, Any>): BotAction {
            val raw = map["equipment"] as? Map<String, Any>
                ?: error("switch_setup 'equipment' must be a map in $map.")
            val setup = Condition.parse(listOf("equipment" to listOf(raw)), "SwitchSetupParser").single()
            val equipment = (setup as content.bot.behaviour.condition.BotEquipmentSetup).items
            val condition = requirement(map, "if").singleOrNull()
            return BotSwitchSetup(equipment, condition)
        }
    }

    object RepositionParser : ActionParser() {
        override val optional = setOf("radius", "if")

        override fun parse(map: Map<String, Any>): BotAction {
            val radius = map["radius"] as? Int ?: 1
            val condition = requirement(map, "if").singleOrNull()
            return BotReposition(radius, condition)
        }
    }

    object RetreatParser : ActionParser() {
        override val required = setOf("safe_area", "regroup_hp_percent")
        override val optional = setOf("if")

        override fun parse(map: Map<String, Any>): BotAction {
            val safeArea = map["safe_area"] as String
            val regroup = map["regroup_hp_percent"] as Int
            val condition = requirement(map, "if").singleOrNull()
            return BotRetreat(safeArea, regroup, condition)
        }
    }

    object InterfaceParser : ActionParser() {
        override val required = setOf("option", "id")
        override val optional = setOf("success", "if")

        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as String
            val id = map["id"] as String
            val success = requirement(map, "success").singleOrNull()
            val condition = requirement(map, "if").singleOrNull()
            return BotInterfaceOption(option, id, success, condition)
        }
    }

    object JewelleryTeleportParser : ActionParser() {
        override val required = setOf("item", "area")
        override val optional = setOf("if", "success")

        override fun parse(map: Map<String, Any>): BotAction {
            val item = map["item"] as String
            val area = map["area"] as String
            val condition = requirement(map, "if").singleOrNull()
            val success = requirement(map, "success").singleOrNull()
            return BotJewelleryTeleport(item, area, condition, success)
        }
    }

    object Firemaking : ActionParser() {
        override val required = setOf("id", "area")

        override fun parse(map: Map<String, Any>): BotAction {
            val area = map["area"] as String
            val id = map["id"] as String
            return BotFiremaking(id, area)
        }
    }

    object CloseInterfaceParser : ActionParser() {
        override val required = setOf("id")
        override fun parse(map: Map<String, Any>): BotAction = BotCloseInterface
    }

    object DialogueParser : ActionParser() {
        override val required = setOf("id")
        override val optional = setOf("option", "success")

        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as? String ?: ""
            val id = map["id"] as String
            val success = requirement(map, "success").singleOrNull()
            return BotDialogueContinue(option, id, success)
        }
    }

    object ItemOnObjectParser : ActionParser() {
        override val required = setOf("id", "object")
        override val optional = setOf("success", "delay")

        override fun parse(map: Map<String, Any>): BotAction {
            val id = map["id"] as String
            val obj = map["object"] as String
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            return BotItemOnObject(id, obj, delay, success)
        }
    }

    object ItemOnItemParser : ActionParser() {
        override val required = setOf("id", "on")
        override val optional = setOf("success")

        override fun parse(map: Map<String, Any>): BotAction {
            val id = map["id"] as String
            val item = map["on"] as String
            val success = requirement(map, "success").singleOrNull()
            return BotItemOnItem(id, item, success)
        }
    }

    object InteractObjectParser : ActionParser() {
        override val required = setOf("option", "id", "success")
        override val optional = setOf("delay", "radius", "x", "y", "if")

        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as String
            val id = map["id"] as String
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            val radius = map["radius"] as? Int ?: 10
            val x = map["x"] as? Int
            val y = map["y"] as? Int
            val condition = requirement(map, "if").singleOrNull()
            return BotInteractObject(option, id, delay, success, radius, x, y, condition)
        }
    }

    object InteractFloorItemParser : ActionParser() {
        override val required = setOf("option", "id", "success")
        override val optional = setOf("delay", "radius", "x", "y")

        override fun parse(map: Map<String, Any>): BotAction {
            val option = map["option"] as String
            val id = map["id"] as String
            val delay = map["delay"] as? Int ?: 0
            val success = requirement(map, "success").singleOrNull()
            val radius = map["radius"] as? Int ?: 10
            val x = map["x"] as? Int
            val y = map["y"] as? Int
            return BotInteractFloorItem(option, id, delay, success, radius, x, y)
        }
    }

    object GoToParser : ActionParser() {
        override val optional = setOf("area", "nearest")
        override fun parse(map: Map<String, Any>) = when {
            map.containsKey("area") -> BotGoTo(map["area"] as String)
            map.containsKey("nearest") -> BotGoToNearest(map["nearest"] as String)
            else -> error("Expected field 'area' or 'nearest', but got '${map["area"]}'")
        }
    }

    object WalkToParser : ActionParser() {
        override val required = setOf("x", "y")
        override val optional = setOf("radius")
        override fun parse(map: Map<String, Any>) = BotWalkTo(map["x"] as Int, map["y"] as Int, map["radius"] as? Int ?: 4)
    }

    object WaitParser : ActionParser() {
        override val required = setOf("ticks")
        override fun parse(map: Map<String, Any>) = BotWait(map["ticks"] as Int)
    }

    object EnterParser : ActionParser() {
        override val optional = setOf("int", "string")
        override fun parse(map: Map<String, Any>) = when {
            map.containsKey("int") -> BotIntEntry(map["int"] as Int)
            map.containsKey("string") -> BotStringEntry(map["string"] as String)
            else -> error("Expected field 'int' or 'string', but got '${map["area"]}'")
        }
    }

    object RestartParser : ActionParser() {
        override val required = setOf("success")
        override val optional = setOf("wait_if")

        @Suppress("UNCHECKED_CAST")
        override fun parse(map: Map<String, Any>): BotAction {
            val requirement = requirement(map, "success").single()
            val wait = map["wait_if"]
            val waitIf = if (wait is List<*>) {
                val requirements = mutableListOf<Condition>()
                wait as List<Map<String, Any>>
                for (element in wait) {
                    requirements.addAll(requirement(element))
                }
                requirements
            } else {
                requirement(map, "wait_if")
            }
            return BotRestart(wait = waitIf, success = requirement)
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        private fun requirement(map: Map<String, Any>, key: String): List<Condition> {
            val value = map[key] ?: return listOf()
            val parent = value as? Map<String, Any> ?: error("Expected map for key $key but found $map")
            return requirement(parent)
        }

        @Suppress("UNCHECKED_CAST")
        private fun requirement(map: Map<String, Any>): List<Condition> {
            val key = map.keys.singleOrNull() ?: error("Collection $map has more than one element.")
            val value = map[key] ?: return listOf()
            val list = when (value) {
                is Map<*, *> -> listOf(value as Map<String, Any>)
                is List<*> -> value as List<Map<String, Any>>
                else -> return listOf()
            }
            return Condition.parse(listOf(key to list), "ActionParser.$key in $map")
        }

        fun parse(list: List<Pair<String, Map<String, Any>>>, name: String): List<BotAction> {
            val actions = mutableListOf<BotAction>()
            for ((type, map) in list) {
                val parser = parsers[type] ?: error("No action parser for '$type' in $name.")
                val error = parser.check(map)
                if (error != null) {
                    error("Action '$type' $error in $name.")
                }
                val action = parser.parse(map)
                actions.add(action)
            }
            return actions
        }

        private val parsers = mapOf(
            "npc" to InteractNpcParser,
            "player" to InteractPlayerParser,
            "object" to InteractObjectParser,
            "floor_item" to InteractFloorItemParser,
            "item_on_object" to ItemOnObjectParser,
            "item_on_item" to ItemOnItemParser,
            "go_to" to GoToParser,
            "tile" to WalkToParser,
            "wait" to WaitParser,
            "restart" to RestartParser,
            "interface" to InterfaceParser,
            "jewellery_teleport" to JewelleryTeleportParser,
            "pray" to PrayParser,
            "spec_attack" to SpecAttackParser,
            "drink_potion" to DrinkPotionParser,
            "cast_vengeance" to CastVengeanceParser,
            "cast_spell" to CastSpellParser,
            "switch_setup" to SwitchSetupParser,
            "retreat" to RetreatParser,
            "reposition" to RepositionParser,
            "interface_close" to CloseInterfaceParser,
            "continue" to DialogueParser,
            "enter" to EnterParser,
            "firemaking" to Firemaking,
        )
    }
}
