package content.bot.action

import content.bot.fact.FactClone
import content.bot.fact.FactReference
import content.bot.fact.HasInventorySpace
import content.bot.fact.CarriesItem
import content.bot.fact.Fact
import content.bot.fact.EquipsItem
import content.bot.fact.AtLocation
import content.bot.fact.OwnsItem
import content.bot.fact.HasSkillLevel
import content.bot.fact.AtTile
import content.bot.fact.HasVariable
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.timedLoad

/**
 * An activity with a limited number of slots that bots can perform
 * E.g. cutting oak trees in varrock, mining copper ore in lumbridge
 */
data class BotActivity(
    override val id: String,
    val capacity: Int,
    override val requirements: List<Fact> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
) : Behaviour

fun loadActivities(paths: List<String>): Map<String, BotActivity> {
    val activities = mutableMapOf<String, BotActivity>()
    val fragments = mutableMapOf<String, BehaviourFragment>()
    timedLoad("bot activity") {
        val clones = mutableMapOf<String, String>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var capacity = 0
                    var template: String? = null
                    var type = "activity"
                    var weight = 0
                    var actions: List<BotAction> = emptyList()
                    var requirements: List<Fact> = emptyList()
                    var fields: Map<String, Any> = emptyMap()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements = requirements()
                            "plan" -> actions = actions()
                            "produces" -> produces()
                            "capacity" -> capacity = int()
                            "type" -> type = string()
                            "template" -> template = string()
                            "weight" -> weight = int()
                            "fields" -> fields = fields()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    val clone = requirements.filterIsInstance<FactClone>().firstOrNull()
                    if (clone != null) {
                        clones[id] = clone.id
                    }
                    if (template != null) {
                        fragments[id] = BehaviourFragment(id, capacity, template, requirements, plan = actions, fields = fields)
                    } else {
                        activities[id] = BotActivity(id, capacity, requirements, plan = actions)
                    }
                }
            }
        }
        // Resolve cloning first
        for (activity in activities.values + fragments.values) {
            for (index in activity.plan.indices.reversed()) {
                val action = activity.plan[index]
                if (action is BotAction.Clone) {
                    val list = activities[action.id]?.plan ?: throw IllegalArgumentException("Unable to find activity to clone '${action.id}'.")
                    val actions = activity.plan as MutableList<BotAction>
                    actions.removeAt(index)
                    actions.addAll(index, list)
                }
            }
            for ((id, cloneId) in clones) {
                val activity = activities[id] ?: continue
                val clone = activities[cloneId] ?: continue
                val requirements = activity.requirements as MutableList<Fact>
                requirements.removeIf { it is FactClone && it.id == cloneId }
                requirements.addAll(clone.requirements)
                requirements.sortBy { it.priority }
            }
        }
        // Fragments are partially filled behaviours with template + fields
        // This code resolves those fields into actual values taken from the template.
        val templates = mutableSetOf<String>()
        for ((id, fragment) in fragments) {
            val template = activities[fragment.template] ?: throw IllegalArgumentException("Unable to find template '${fragment.template}' for activity '$id'.")
            templates.add(fragment.template)

            val requirements = mutableListOf<Fact>()
            requirements.addAll(fragment.requirements)
            fragment.resolveRequirements(template, requirements)
            requirements.sortBy { it.priority }

            val actions = mutableListOf<BotAction>()
            actions.addAll(fragment.plan)
            fragment.resolveActions(template, actions)
            activities[id] = BotActivity(id, fragment.capacity, requirements, actions)
        }
        // Templates aren't selectable activities
        for (template in templates) {
            activities.remove(template)
        }
        activities.size
    }
    return activities
}

private fun ConfigReader.produces() {
    while (nextElement()) {
        while (nextEntry()) {
            val key = key()
            val value = value()
//            println("$key = $value")
        }
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

private fun ConfigReader.requirements(): List<Fact> {
    val list = mutableListOf<Fact>()
    while (nextElement()) {
        var type = ""
        var id = ""
        var value: Any? = null
        var min = 1
        var max = 1
        var x = 0
        var y = 0
        var level = 0
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
                "x" -> {
                    type = "tile"
                    when (val value = value()) {
                        is Int -> x = value
                        is String if value.contains('$') -> references[key] = value
                        else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                    }
                }
                "y" -> {
                    type = "tile"
                    when (val value = value()) {
                        is Int -> y = value
                        is String if value.contains('$') -> references[key] = value
                        else -> throw IllegalArgumentException("Invalid '$key' value: $value ${exception()}")
                    }
                }
                "level" -> {
                    type = "tile"
                    when (val value = value()) {
                        is Int -> level = value
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
        var requirement = when (type) {
            "skill" -> HasSkillLevel(id, min, max)
            "carries" -> CarriesItem(id, min)
            "owns" -> OwnsItem(id, min)
            "equips" -> EquipsItem(id, min)
            "variable" -> HasVariable(id, value)
            "clone" -> FactClone(id)
            "inventory_space" -> HasInventorySpace(min)
            "location" -> AtLocation(id)
            "tile" -> AtTile(x, y, level, min)
            "holds" -> throw IllegalArgumentException("Unknown requirement type 'holds'; did you mean 'carries' or 'equips'? ${exception()}.")
            else -> throw IllegalArgumentException("Unknown requirement type: $type ${exception()}")
        }
        if (references.isNotEmpty()) {
            requirement = FactReference(requirement, references)
        }
        list.add(requirement)
    }
    list.sortBy { it.priority }
    return list
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