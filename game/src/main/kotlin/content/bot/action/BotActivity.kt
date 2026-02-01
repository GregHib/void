package content.bot.action

import content.bot.fact.Condition
import content.bot.fact.Fact
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.timedLoad

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

fun loadActivities(paths: List<String>, activities: MutableMap<String, BotActivity>, groups: MutableMap<String, MutableList<String>>, resolvers: MutableMap<String, MutableList<Resolver>>) {
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
                    var actions: List<BotAction> = emptyList()
                    val requirements: MutableList<Condition> = mutableListOf()
                    val resolvables: MutableList<Condition> = mutableListOf()
                    val produces: MutableList<Condition> = mutableListOf()
                    var fields: Map<String, Any> = emptyMap()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requirements) // TODO convert to pass mutable list + clone list
                            "resolve" -> requirements(resolvables)
                            "actions" -> actions = actions()
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
                        fragments[id] = BehaviourFragment(id, type, capacity, weight, template, requirements, resolvables, actions = actions, fields = fields)
                    } else if (type == "resolver") {
                        for (fact in produces) {
                            for (key in fact.keys()) {
                                resolvers.getOrPut(key) { mutableListOf() }.add(Resolver(id, weight, requirements, resolvables, actions = actions))
                            }
                        }
                    } else {
                        activities[id] = BotActivity(id, capacity, requirements, resolvables, actions = actions)
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

            val actions = mutableListOf<BotAction>()
            actions.addAll(fragment.actions)
            fragment.resolveActions(template, actions)
            if (fragment.type == "resolver") {
                for (fact in fragment.produces) {
                    for (key in fact.keys()) {
                        resolvers.getOrPut(key) { mutableListOf() }.add(Resolver(id, fragment.weight, requirements, resolvables, actions))
                    }
                }
            } else {
                activities[id] = BotActivity(id, fragment.capacity, requirements, resolvables, actions)
            }
        }
        // Templates aren't selectable activities
        for (template in templates) {
            activities.remove(template)
        }
        // Group activities by requirement types
        for (activity in activities.values) {
            for (fact in activity.requires) {
                for (key in fact.keys()) {
                    groups.getOrPut(key) { mutableListOf() }.add(activity.id)
                }
            }
            println(activity)
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

private fun ConfigReader.requirements(list: MutableList<Condition>) {
    while (nextElement()) {
        var type = ""
        var id = ""
        var value: Any? = null
        var min: Int? = null
        var max: Int? = null
        val references = mutableMapOf<String, String>()
        while (nextEntry()) {
            when (val key = key()) {
                "skill", "carries", "equips", "owns", "variable", "clone", "location" -> {
                    type = key
                    id = string()
                    if (id.contains('$')) {
                        references[key] = id
                    }
                }
                "amount" -> when (val value = value()) {
                    is Int -> {
                        min = value
                        max = value
                    }
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "min" -> when (val value = value()) {
                    is Int -> min = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "max" -> when (val value = value()) {
                    is Int -> max = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "inventory_space" -> {
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
            }
        }
        var requirement = getRequirement(type, id, min, max, value)
        if (requirement == null) {
            if (type == "holds") {
                throw IllegalArgumentException("Unknown requirement type 'holds'; did you mean 'carries' or 'equips'? ${exception()}.")
            }
            throw IllegalArgumentException("Unknown requirement type: $type ${exception()}")
        }
        if (references.isNotEmpty()) {
            requirement = Condition.Reference(type, id, value, min, max, references)
        }
        list.add(requirement)
    }
    list.sortBy { it.priority() }
}

private fun getRequirement(type: String, id: String, min: Int?, max: Int?, value: Any?): Condition? = when (type) {
    "skill" -> Condition.range(Fact.SkillLevel.of(id), min, max)
    "carries" -> if (id.contains(",")) {
        Condition.Any(id.split(",").map { Condition.range(Fact.InventoryCount(it), min, max) })
    } else if (id.any { it == '*' || it == '#' }) {
        Condition.Any(Wildcards.get(id, Wildcard.Item).map { Condition.range(Fact.InventoryCount(it), min, max) })
    } else {
        Condition.range(Fact.InventoryCount(id), min, max)
    }
    "equips" -> if (id.contains(",")) {
        Condition.Any(id.split(",").map { Condition.range(Fact.EquipCount(it), min, max) })
    } else if (id.any { it == '*' || it == '#' }) {
        Condition.Any(Wildcards.get(id, Wildcard.Item).map { Condition.range(Fact.EquipCount(it), min, max) })
    } else {
        Condition.range(Fact.EquipCount(id), min, max)
    }
    "variable" -> when(value) {
        is Int -> Condition.Equals(Fact.IntVariable(id), value)
        is String -> Condition.Equals(Fact.StringVariable(id), value)
        is Double -> Condition.Equals(Fact.DoubleVariable(id), value)
        is Boolean -> Condition.Equals(Fact.BoolVariable(id), value)
        else -> null
    }
    "clone" -> Condition.Clone(id)
    "inventory_space" -> Condition.range(Fact.InventorySpace, min, max)
    "location" -> Condition.Area(Fact.PlayerTile, id)
    else -> null
}

private fun ConfigReader.actions(): List<BotAction> {
    val list = mutableListOf<BotAction>()
    while (nextElement()) {
        var type = ""
        var id = ""
        var option = ""
        var retryTicks = 0
        var retryMax = 0
        var timeout = 0
        var ticks = 0
        var radius = 10
        var x = 0
        var y = 0
        val references = mutableMapOf<String, String>()
        while (nextEntry()) {
            when (val key = key()) {
                "go_to", "wait_for", "interface", "npc", "object", "clone" -> {
                    type = key
                    id = string()
                    if (id.contains('$')) {
                        references[key] = id
                    }
                }
                "x" -> {
                    type = "tile"
                    val value = string()
                    if (value.contains('$')) {
                        references[key] = value
                    } else {
                        x = value.toInt()
                    }
                }
                "y" -> {
                    type = "tile"
                    val value = string()
                    if (value.contains('$')) {
                        references[key] = value
                    } else {
                        y = value.toInt()
                    }
                }
                "target", "id" -> {
                    id = string()
                    if (id.contains('$')) {
                        references[key] = id
                    }
                }
                "option" -> {
                    option = string()
                    if (option.contains('$')) {
                        references[key] = option
                    }
                }
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
                "retry_ticks" -> when (val value = value()) {
                    is Int -> retryTicks = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                "retry_max" -> when (val value = value()) {
                    is Int -> retryMax = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")

                }
                "timeout" -> when (val value = value()) {
                    is Int -> timeout = value
                    is String if value.contains('$') -> references[key] = value
                    else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                }
                else -> throw IllegalArgumentException("Unknown action key: $key ${exception()}")
            }
        }
        var action = when (type) {
            "go_to" -> BotAction.GoTo(id)
            "wait" -> BotAction.Wait(ticks)
            "npc" -> BotAction.InteractNpc(id = id, option = option, retryTicks = retryTicks, retryMax = retryMax, radius = radius)
            "tile" -> BotAction.WalkTo(x = x, y = y)
            "object" -> BotAction.InteractObject(id = id, option = option, retryTicks = retryTicks, retryMax = retryMax, radius = radius)
            "interface" -> BotAction.InterfaceOption(id = id, option = option)
            "clone" -> BotAction.Clone(id)
            "wait_for" -> when (id) {
                "full_inventory" -> BotAction.WaitFullInventory(timeout)
                else -> throw IllegalArgumentException("Unknown wait_for action: $id ${exception()}")
            }
            else -> throw IllegalArgumentException("Unknown action type: $type ${exception()}")
        }
        if (references.isNotEmpty()) {
            action = BotAction.Reference(action, references)
        }
        list.add(action)
    }
    return list
}