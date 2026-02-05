package content.bot.action

import content.bot.fact.Condition
import content.bot.fact.Fact
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.timedLoad
import kotlin.math.min

/**
 * An activity with a limited number of slots that bots can perform
 * E.g. cutting oak trees in varrock, mining copper ore in lumbridge
 */
data class BotActivity(
    override val id: String,
    val capacity: Int,
    override val requires: List<Condition> = emptyList(),
    override val resolve: List<Condition> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Condition> = emptySet(),
) : Behaviour

fun loadActivities(
    paths: List<String>,
    activities: MutableMap<String, BotActivity>,
    groups: MutableMap<String, MutableList<String>>,
    resolvers: MutableMap<String, MutableList<Resolver>>,
    shortcuts: MutableList<NavigationShortcut>,
) {
    val fragments = mutableMapOf<String, BehaviourFragment>()
    timedLoad("bot activity") {
        val reqClones = mutableMapOf<String, String>()
        val resClones = mutableMapOf<String, String>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var capacity = 0
                    var template: String? = null
                    var type = "activity"
                    var weight = 0
                    val actions: MutableList<BotAction> = mutableListOf()
                    val requirements: MutableList<Condition> = mutableListOf()
                    val resolvables: MutableList<Condition> = mutableListOf()
                    val produces: MutableList<Condition> = mutableListOf()
                    var fields: Map<String, Any> = emptyMap()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requirements)
                            "setup" -> requirements(resolvables)
                            "actions" -> actions(actions)
                            "produces" -> requirements(produces)
                            "capacity" -> capacity = int()
                            "type" -> type = string()
                            "template" -> template = string()
                            "weight" -> weight = int()
                            "fields" -> fields = fields()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    val clone = requirements.filterIsInstance<Condition.Clone>().firstOrNull()
                    if (clone != null) {
                        reqClones[id] = clone.id
                    }
                    val resolveClone = resolvables.filterIsInstance<Condition.Clone>().firstOrNull()
                    if (resolveClone != null) {
                        resClones[id] = resolveClone.id
                    }

                    if (template != null) {
                        fragments[id] = BehaviourFragment(id, type, capacity, weight, template, requirements, resolvables, actions = actions, fields = fields, produces = produces.toSet())
                    } else if (type == "resolver") {
                        val resolver = Resolver(id, weight, requirements, resolvables, actions = actions, produces = produces.toSet())
                        for (fact in produces) {
                            for (key in fact.keys()) {
                                resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
                            }
                        }
                    } else if (type == "shortcut") {
                        require(resolvables.isEmpty()) { "Shortcuts cannot have setup requirements" }
                        shortcuts.add(NavigationShortcut(id, weight, requirements, actions = actions, produces = produces.toSet()))
                    } else {
                        activities[id] = BotActivity(id, capacity, requirements, resolvables, actions = actions, produces = produces.toSet())
                    }
                }
            }
        }
        // Resolve cloning first
        for (activity in activities.values + fragments.values) {
            for (index in activity.actions.indices.reversed()) {
                val action = activity.actions[index]
                if (action is BotAction.Clone) {
                    val list = activities[action.id]?.actions ?: throw IllegalArgumentException("Unable to find activity to clone '${action.id}'.")
                    val actions = activity.actions as MutableList<BotAction>
                    actions.removeAt(index)
                    actions.addAll(index, list)
                }
            }
            for ((id, cloneId) in reqClones) {
                val activity = activities[id] ?: continue
                val clone = activities[cloneId] ?: continue
                val requirements = activity.requires as MutableList<Condition>
                requirements.removeIf { it is Condition.Clone && it.id == cloneId }
                requirements.addAll(clone.requires)
                requirements.sortBy { it.priority() }
            }
            for ((id, cloneId) in resClones) {
                val activity = activities[id] ?: continue
                val clone = activities[cloneId] ?: continue
                val resolvables = activity.resolve as MutableList<Condition>
                resolvables.removeIf { it is Condition.Clone && it.id == cloneId }
                resolvables.addAll(clone.resolve)
                resolvables.sortBy { it.priority() }
            }
        }
        // Fragments are partially filled behaviours with template + fields
        // This code resolves those fields into actual values taken from the template.
        val templates = mutableSetOf<String>()
        for ((id, fragment) in fragments) {
            val template = activities[fragment.template] ?: throw IllegalArgumentException("Unable to find template '${fragment.template}' for activity '$id'.")
            templates.add(fragment.template)

            val requirements = mutableListOf<Condition>()
            requirements.addAll(fragment.requires)
            fragment.resolveRequirements(requirements, template.requires)
            requirements.sortBy { it.priority() }

            val resolvables = mutableListOf<Condition>()
            resolvables.addAll(fragment.resolve)
            fragment.resolveRequirements(resolvables, template.resolve)
            resolvables.sortBy { it.priority() }

            val products = mutableListOf<Condition>()
            products.addAll(fragment.produces)
            fragment.resolveRequirements(products, template.produces.toList())
            products.sortBy { it.priority() }

            val actions = mutableListOf<BotAction>()
            actions.addAll(fragment.actions)
            fragment.resolveActions(template, actions)
            when (fragment.type) {
                "resolver" -> {
                    val resolver = Resolver(id, fragment.weight, requirements, resolvables, actions, products.toSet())
                    for (fact in products) {
                        for (key in fact.keys()) {
                            resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
                        }
                    }
                }
                "shortcut" -> shortcuts.add(NavigationShortcut(id, fragment.weight, requirements, actions = actions))
                else -> activities[id] = BotActivity(id, template.capacity, requirements, resolvables, actions)
            }
        }
        // Templates aren't selectable activities
        for (template in templates) {
            activities.remove(template)
        }
        // Group activities by requirement types
        for (activity in activities.values) {
            for (fact in activity.requires) {
                for (key in fact.groups()) {
                    groups.getOrPut(key) { mutableListOf() }.add(activity.id)
                }
            }
        }
        activities.size
    }
}

private fun ConfigReader.fields(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    while (nextEntry()) {
        val key = key()
        val value = value()
        map[key] = value
    }
    return map
}

fun ConfigReader.requirements(list: MutableList<Condition>) {
    while (nextElement()) {
        list.add(requirement())
    }
    list.sortBy { it.priority() }
}

private fun ConfigReader.requirement(exact: Boolean = false): Condition {
    var type = ""
    var id = ""
    var value: Any? = null
    var default: Any? = null
    var min: Int? = null
    var max: Int? = null
    val references = mutableMapOf<String, String>()
    while (nextEntry()) {
        when (val key = key()) {
            "skill", "carries", "equips", "interface", "owns", "clock", "variable", "clone", "location" -> {
                type = key
                id = string()
                if (id.contains('$')) {
                    references[key] = id
                }
            }
            "amount", "min" -> when (val value = value()) {
                is Int -> min = value
                is String if value.contains('$') -> references[key] = value
                else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
            }
            "max" -> when (val value = value()) {
                is Int -> max = value
                is String if value.contains('$') -> references[key] = value
                else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
            }
            "combat_level", "inventory_space" -> {
                type = key
                when (val value = value()) {
                    is Int -> min = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
            }
            "radius" -> {
                type = "tile"
                when (val value = value()) {
                    is Int -> min = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
            }
            "value" -> value = value()
            "default" -> default = value()
        }
    }
    var requirement = getRequirement(type, id, min, max, value, default, exact)
    if (requirement == null) {
        if (type == "holds") {
            throw IllegalArgumentException("Unknown requirement type 'holds'; did you mean 'carries' or 'equips'? ${exception()}.")
        }
        throw IllegalArgumentException("Unknown requirement type: $type ${exception()}")
    }
    if (references.isNotEmpty()) {
        requirement = Condition.Reference(type, id, value, default, min, max, references)
    }
    return requirement
}

private fun getRequirement(type: String, id: String, min: Int?, max: Int?, value: Any?, default: Any?, exact: Boolean): Condition? = when (type) {
    "skill" -> Condition.range(Fact.SkillLevel.of(id), min, max)
    "carries" -> Condition.split(id, min, max, Wildcard.Item) { Fact.InventoryCount(it) }
    "owns" -> Condition.split(id, min, max, Wildcard.Item) { Fact.ItemCount(it) }
    "banked" -> Condition.split(id, min, max, Wildcard.Item) { Fact.BankCount(it) }
    "equips" -> Condition.split(id, min, max, Wildcard.Item) { Fact.EquipCount(it) }
    "clock" -> Condition.split(id, min, max, Wildcard.Variables) { Fact.ClockRemaining(it) }
    "timer" -> Condition.Equals(Fact.HasTimer(id), value as? Boolean ?: true)
    "interface" -> Condition.Equals(Fact.InterfaceOpen(id), value as? Boolean ?: true)
    "variable" -> when (value) {
        is Int -> Condition.Equals(Fact.IntVariable(id, default as? Int), value)
        is String -> Condition.Equals(Fact.StringVariable(id, default as? String), value)
        is Double -> Condition.Equals(Fact.DoubleVariable(id, default as? Double), value)
        is Boolean -> Condition.Equals(Fact.BoolVariable(id, default as? Boolean), value)
        else -> null
    }
    "clone" -> Condition.Clone(id)
    "inventory_space" -> if (exact && min != null) Condition.Equals(Fact.InventorySpace, min) else Condition.range(Fact.InventorySpace, min, max)
    "location" -> Condition.Area(Fact.PlayerTile, id)
    "combat_level" -> Condition.AtLeast(Fact.CombatLevel, min ?: 1)
    else -> null
}

fun ConfigReader.actions(list: MutableList<BotAction>) {
    while (nextElement()) {
        var type = ""
        var id = ""
        var option = ""
        var int = 0
        var ticks = 0
        var radius = 10
        var delay = 0
        var heal = 0
        var loot = 0
        var x = 0
        var y = 0
        var success: Condition? = null
        val references = mutableMapOf<String, String>()
        while (nextEntry()) {
            when (val key = key()) {
                "go_to", "go_to_nearest", "enter_string", "interface", "npc", "object", "clone", "item", "continue" -> {
                    type = key
                    id = string()
                    if (id.contains('$')) {
                        references[key] = id
                    }
                }
                "x" -> {
                    if (type == "") {
                        type = "tile"
                    }
                    val value = value()
                    if (value is String && value.contains('$')) {
                        references[key] = value
                    } else {
                        x = value as Int
                    }
                }
                "y" -> {
                    if (type == "") {
                        type = "tile"
                    }
                    val value = value()
                    if (value is String && value.contains('$')) {
                        references[key] = value
                    } else {
                        y = value as Int
                    }
                }
                "target", "id" -> {
                    id = string()
                    if (id.contains('$')) {
                        references[key] = id
                    }
                }
                "option", "on" -> {
                    option = string()
                    if (option.contains('$')) {
                        references[key] = option
                    }
                }
                "success" -> success = requirement(exact = true)
                "wait" -> {
                    type = key
                    when (val value = value()) {
                        is Int -> ticks = value
                        is String if value.contains('$') -> references[key] = value
                        else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                    }
                }
                "radius" -> when (val value = value()) {
                    is Int -> radius = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "heal_percent" -> when (val value = value()) {
                    is Int -> heal = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "loot_over" -> when (val value = value()) {
                    is Int -> loot = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "delay" -> when (val value = value()) {
                    is Int -> delay = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "enter_int" -> {
                    type = key
                    when (val value = value()) {
                        is Int -> int = value
                        is String if value.contains('$') -> references[key] = value
                        else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                    }
                }
                else -> throw IllegalArgumentException("Unknown action key: $key ${exception()}")
            }
        }
        var action = when (type) {
            "go_to" -> BotAction.GoTo(id)
            "go_to_nearest" -> BotAction.GoToNearest(id)
            "enter_string" -> BotAction.StringEntry(id)
            "enter_int" -> BotAction.IntEntry(int)
            "wait" -> BotAction.Wait(ticks)
            "npc" -> if (option == "Attack") {
                BotAction.FightNpc(id = id, delay = delay, success = success, healPercentage = heal, lootOverValue = loot, radius = radius)
            } else {
                BotAction.InteractNpc(id = id, option = option, delay = delay, success = success, radius = radius)
            }
            "tile" -> BotAction.WalkTo(x = x, y = y)
            "object" -> BotAction.InteractObject(id = id, option = option, delay = delay, success = success, radius = radius)
            "interface" -> BotAction.InterfaceOption(id = id, option = option)
            "continue" -> BotAction.DialogueContinue(id = id, option = option)
            "item" -> BotAction.ItemOnItem(item = id, on = option)
            "clone" -> BotAction.Clone(id)
            else -> throw IllegalArgumentException("Unknown action type: $type ${exception()}")
        }
        if (references.isNotEmpty()) {
            action = BotAction.Reference(action, references)
        }
        list.add(action)
    }
}