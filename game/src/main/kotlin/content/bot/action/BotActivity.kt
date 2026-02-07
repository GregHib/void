package content.bot.action

import content.bot.fact.Condition
import content.bot.fact.Requirement
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import kotlin.collections.set

/**
 * An activity with a limited number of slots that bots can perform
 * E.g. cutting oak trees in varrock, mining copper ore in lumbridge
 */
data class BotActivity(
    override val id: String,
    val capacity: Int,
    override val requires: List<Requirement<*>> = emptyList(),
    override val setup: List<Requirement<*>> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Requirement<*>> = emptySet(),
) : Behaviour

fun loadActivities(
    files: ConfigFiles,
    activities: MutableMap<String, BotActivity>,
    groups: MutableMap<String, MutableList<String>>,
    resolvers: MutableMap<String, MutableList<Resolver>>,
    shortcuts: MutableList<NavigationShortcut>,
) {
    val templates = loadTemplates(files.list(Settings["bots.templates"]))
    loadActivities(activities, templates, files.list(Settings["bots.definitions"]))
    // Group activities by requirement types
    for (activity in activities.values) {
        for (req in activity.requires) {
            for (key in req.fact.groups()) {
                groups.getOrPut(key) { mutableListOf() }.add(activity.id)
            }
        }
    }
    loadSetups(resolvers, templates, files.list(Settings["bots.setups"]))
    loadShortcuts(shortcuts, templates, files.list(Settings["bots.shortcuts"]))
}

private fun loadActivities(activities: MutableMap<String, BotActivity>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot activity") {
        val fragments = mutableListOf<Fragment>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var template: String? = null
                    var fields: Map<String, Any>? = null
                    var capacity = 1
                    val requires = mutableListOf<Pair<String, Map<String, Any>>>()
                    val setup = mutableListOf<Pair<String, Map<String, Any>>>()
                    val actions = mutableListOf<BotAction>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "produces" -> requirements(produces)
                            "capacity" -> capacity = int()
                            "fields" -> fields = map()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (template != null) {
                        requireNotNull(fields)
                        fragments.add(Fragment(id, template, fields, capacity, requires, setup, actions, produces))
                    } else {
                        val debug = "$id ${exception()}"
                        activities[id] = BotActivity(id, capacity, Requirement.parse(requires, debug), Requirement.parse(setup, debug), actions, Requirement.parse(produces, debug, requirePredicates = false).toSet())
                    }
                }
            }
        }

        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            activities[fragment.id] = fragment.activity(template)
        }
        for (activity in activities.values) {
            println(activity)
        }
        activities.size
    }
}

private fun loadSetups(resolvers: MutableMap<String, MutableList<Resolver>>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot setup") {
        val fragments = mutableListOf<Fragment>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var template: String? = null
                    var fields: Map<String, Any>? = null
                    var weight = 1
                    val requires = mutableListOf<Pair<String, Map<String, Any>>>()
                    val setup = mutableListOf<Pair<String, Map<String, Any>>>()
                    val actions = mutableListOf<BotAction>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "produces" -> requirements(produces)
                            "weight" -> weight = int()
                            "fields" -> fields = map()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (template != null) {
                        requireNotNull(fields)
                        fragments.add(Fragment(id, template, fields, weight, requires, setup, actions, produces))
                    } else {
                        val debug = "$id ${exception()}"
                        val products = Requirement.parse(produces, debug)
                        val resolver = Resolver(id, weight, Requirement.parse(requires, debug), Requirement.parse(setup, debug), actions, products.toSet())
                        for (product in products) {
                            for (key in product.fact.keys()) {
                                resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
                            }
                        }
                    }
                }
            }
        }

        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            resolvers.getOrPut(fragment.id) { mutableListOf() }.add(fragment.resolver(template))
        }
        resolvers.size
    }
}

private fun loadShortcuts(shortcuts: MutableList<NavigationShortcut>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot shortcut") {
        val fragments = mutableListOf<Fragment>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var template: String? = null
                    var fields: Map<String, Any>? = null
                    var weight = 1
                    val requires = mutableListOf<Pair<String, Map<String, Any>>>()
                    val setup = mutableListOf<Pair<String, Map<String, Any>>>()
                    val actions = mutableListOf<BotAction>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "produces" -> requirements(produces)
                            "weight" -> weight = int()
                            "fields" -> fields = map()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (fields != null && template == null) {
                        error("Found fields but no template for $id in ${exception()}")
                    } else if (template != null) {
                        requireNotNull(fields) { "No fields found for $id ${exception()}"}
                        fragments.add(Fragment(id, template, fields, weight, requires, setup, actions, produces))
                    } else {
                        val debug = "$id ${exception()}"
                        shortcuts.add(NavigationShortcut(id, weight, Requirement.parse(requires, debug), Requirement.parse(setup, debug), actions, Requirement.parse(produces, debug, requirePredicates = false).toSet()))
                    }
                }
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            shortcuts.add(fragment.shortcut(template))
        }
        shortcuts.size
    }
}

private fun loadTemplates(paths: List<String>): Map<String, Template> {
    val templates = mutableMapOf<String, Template>()
    timedLoad("bot template") {
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    val requires = mutableListOf<Pair<String, Map<String, Any>>>()
                    val setup = mutableListOf<Pair<String, Map<String, Any>>>()
                    val actions = mutableListOf<BotAction>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "produces" -> requirements(produces)
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    templates[id] = Template(requires, setup, actions, produces)
                }
            }
        }
        templates.size
    }
    return templates
}

private fun ConfigReader.requirements(requires: MutableList<Pair<String, Map<String, Any>>>) {
    while (nextElement()) {
        while (nextEntry()) {
            val key = key()
            val value = map()
            requires.add(key to value)
        }
    }
}

private data class Fragment(
    val id: String,
    val template: String,
    val fields: Map<String, Any>,
    val int: Int,
    val requires: List<Pair<String, Map<String, Any>>>,
    val setup: List<Pair<String, Map<String, Any>>>,
    val actions: List<BotAction>, // Can fragments even have actions?
    val produces: List<Pair<String, Map<String, Any>>>,
) {
    fun activity(template: Template) = BotActivity(
        id = id,
        capacity = int,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = template.actions,
        produces = resolveRequirements(template.produces, produces, requirePredicates = false).toSet(),
    )

    private fun resolveRequirements(templated: List<Pair<String, Map<String, Any>>>, original: List<Pair<String, Map<String, Any>>>, requirePredicates: Boolean = true): List<Requirement<*>> {
        val combinedList = mutableListOf<Pair<String, Map<String, Any>>>()
        for ((type, map) in templated) {
            val combinedMap = mutableMapOf<String, Any>()
            for ((key, value) in original) {
                combinedMap[key] = value
            }
            for ((key, value) in map) {
                if (value !is String || !value.contains('$')) {
                    combinedMap[key] = value
                    continue
                }
                val ref = value.reference()
                val name = ref.trim('$', '{', '}')
                val replacement = fields[name] ?: error("No field found for behaviour=$id type=${type} key=$key ref=$ref")
                combinedMap[key] = if (replacement is String) value.replace(ref, replacement) else replacement
            }
            if (combinedMap.isNotEmpty()) {
                combinedList.add(type to combinedMap)
            }
        }
        if (combinedList.isEmpty()) {
            return emptyList()
        }
        return Requirement.parse(combinedList, "$id template $template", requirePredicates)
    }

    private fun String.reference(): String {
        if (startsWith('$')) {
            return this
        }
        val index = indexOf($$"${")
        if (index == -1) {
            return "\$${substringAfter('$')}"
        }
        val end = indexOf('}', index) + 1
        return substring(index, end)
    }

    fun resolver(template: Template) = Resolver(
        id = id,
        weight = int,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = template.actions,
        produces = resolveRequirements(template.produces, produces, requirePredicates = false).toSet(),
    )

    fun shortcut(template: Template) = NavigationShortcut(
        id = id,
        weight = int,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = template.actions,
        produces = resolveRequirements(template.produces, produces, requirePredicates = false).toSet(),
    )
}

private data class Template(
    val requires: List<Pair<String, Map<String, Any>>>,
    val setup: List<Pair<String, Map<String, Any>>>,
    val actions: List<BotAction>,
    val produces: List<Pair<String, Map<String, Any>>>,
)

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
        val references = mutableMapOf<String, String>()
        val wait = mutableListOf<Pair<String, Map<String, Any>>>()
        val success = mutableListOf<Pair<String, Map<String, Any>>>()
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
                "on_object" -> {
                    type = "${type}_on_object"
                    option = string()
                    if (option.contains('$')) {
                        references[key] = option
                    }
                }
                "restart" -> {
                    require(boolean()) { "Can't have restart = false ${exception()}" }
                    type = key
                }
                "success" -> while (nextEntry()) {
                    success.add(key() to map())
                }
                "wait_if" -> while (nextElement()) {
                    while (nextEntry()) {
                        wait.add(key() to map())
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
            "restart" -> BotAction.Restart(Requirement.parse(wait, id), Requirement.parse(success, id).singleOrNull() ?: throw IllegalArgumentException("Restart must have success condition."))
            "npc" -> if (option == "Attack") {
                BotAction.FightNpc(id = id, delay = delay, success = Requirement.parse(success, id).singleOrNull(), healPercentage = heal, lootOverValue = loot, radius = radius)
            } else {
                BotAction.InteractNpc(id = id, option = option, delay = delay, success = Requirement.parse(success, id).singleOrNull(), radius = radius)
            }
            "tile" -> BotAction.WalkTo(x = x, y = y)
            "object" -> BotAction.InteractObject(id = id, option = option, delay = delay, success = Requirement.parse(success, id).singleOrNull(), radius = radius)
            "interface" -> BotAction.InterfaceOption(id = id, option = option, success = Requirement.parse(success, id).singleOrNull())
            "continue" -> BotAction.DialogueContinue(id = id, option = option, success = Requirement.parse(success, id).singleOrNull())
            "item" -> BotAction.ItemOnItem(item = id, on = option, success = Requirement.parse(success, id).singleOrNull())
            "item_on_object" -> BotAction.ItemOnObject(item = id, id = option, success = Requirement.parse(success, id).singleOrNull())
            "clone" -> BotAction.Clone(id)
            else -> throw IllegalArgumentException("Unknown action type: $type ${exception()}")
        }
        if (references.isNotEmpty()) {
            action = BotAction.Reference(action, references)
        }
        list.add(action)
    }
}
