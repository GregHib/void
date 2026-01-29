package content.bot.action

import content.bot.fact.FactClone
import content.bot.fact.FactReference
import content.bot.fact.HasInventorySpace
import content.bot.fact.CarriesItem
import content.bot.fact.Fact
import content.bot.fact.EquipsItem
import content.bot.fact.AtLocation
import content.bot.fact.HasSkillLevel
import content.bot.fact.AtTile
import content.bot.fact.CarriesOne
import content.bot.fact.EquipsOne
import content.bot.fact.HasVariable
import net.pearx.kasechange.toPascalCase
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.entity.character.player.skill.Skill
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
    override val requires: List<Fact> = emptyList(),
    override val resolve: List<Fact> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
    override val produces: Set<Fact> = emptySet(),
) : Behaviour

fun loadActivities(paths: List<String>, activities: MutableMap<String, BotActivity>, groups: MutableMap<String, MutableList<String>>, resolvers: MutableMap<Fact, MutableList<Resolver>>) {
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
                    var requirements: List<Fact> = emptyList()
                    var resolvables: List<Fact> = emptyList()
                    var produces: List<Fact> = emptyList()
                    var fields: Map<String, Any> = emptyMap()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements = requirements()
                            "resolve" -> resolvables = requirements()
                            "plan" -> actions = actions()
                            "produces" -> produces = requirements()
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
                        reqClones[id] = clone.id
                    }
                    val resolveClone = resolvables.filterIsInstance<FactClone>().firstOrNull()
                    if (resolveClone != null) {
                        resClones[id] = resolveClone.id
                    }

                    if (template != null) {
                        fragments[id] = BehaviourFragment(id, type, capacity, weight, template, requirements, plan = actions, fields = fields)
                    } else if (type == "resolver") {
                        for (fact in produces) {
                            resolvers.getOrPut(fact) { mutableListOf() }.add(Resolver(id, weight, requirements, plan = actions))
                        }
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
            for ((id, cloneId) in reqClones) {
                val activity = activities[id] ?: continue
                val clone = activities[cloneId] ?: continue
                val requirements = activity.requires as MutableList<Fact>
                requirements.removeIf { it is FactClone && it.id == cloneId }
                requirements.addAll(clone.requires)
                requirements.sortBy { it.priority }
            }
            for ((id, cloneId) in resClones) {
                val activity = activities[id] ?: continue
                val clone = activities[cloneId] ?: continue
                val resolvables = activity.resolve as MutableList<Fact>
                resolvables.removeIf { it is FactClone && it.id == cloneId }
                resolvables.addAll(clone.resolve)
                resolvables.sortBy { it.priority }
            }
        }
        // Fragments are partially filled behaviours with template + fields
        // This code resolves those fields into actual values taken from the template.
        val templates = mutableSetOf<String>()
        for ((id, fragment) in fragments) {
            val template = activities[fragment.template] ?: throw IllegalArgumentException("Unable to find template '${fragment.template}' for activity '$id'.")
            templates.add(fragment.template)

            val requirements = mutableListOf<Fact>()
            requirements.addAll(fragment.requires)
            fragment.resolveRequirements(template, requirements)
            requirements.sortBy { it.priority }

            val resolvables = mutableListOf<Fact>()
            resolvables.addAll(fragment.requires)
            fragment.resolveRequirements(template, resolvables)
            resolvables.sortBy { it.priority }

            val actions = mutableListOf<BotAction>()
            actions.addAll(fragment.plan)
            fragment.resolveActions(template, actions)
            if (fragment.type == "resolver") {
                for (fact in fragment.produces) {
                    resolvers.getOrPut(fact) { mutableListOf() }.add(Resolver(id, fragment.weight, requirements, resolvables, actions))
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
        }
        activities.size
    }
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
            "skill" -> HasSkillLevel(Skill.of(id.toPascalCase())!!, min, max)
            "carries" -> if (id.any { it == '*' || it == '#' }) {
                CarriesOne(Wildcards.get(id, Wildcard.Item), min)
            } else {
                CarriesItem(id, min)
            }
            "equips" -> if (id.any { it == '*' || it == '#' }) {
                EquipsOne(Wildcards.get(id, Wildcard.Item), min)
            } else {
                EquipsItem(id, min)
            }
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