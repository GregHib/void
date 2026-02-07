package content.bot.action

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
                    val actions = mutableListOf<Map<String, Any>>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> while (nextElement()) {
                                actions.add(map())
                            }
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
                        activities[id] = BotActivity(id, capacity, Requirement.parse(requires, "$id ${exception()}"))
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
                    val actions = mutableListOf<Map<String, Any>>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> while (nextElement()) {
                                actions.add(map())
                            }
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
                        val resolver = Resolver(id, weight, Requirement.parse(requires, debug), Requirement.parse(setup, debug), produces = products.toSet())
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
                    val actions = mutableListOf<Map<String, Any>>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "template" -> template = string()
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> while (nextElement()) {
                                actions.add(map())
                            }
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
                        shortcuts.add(NavigationShortcut(id, weight, Requirement.parse(requires, id), Requirement.parse(setup, id)))
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
                    val actions = mutableListOf<Map<String, Any>>()
                    val produces = mutableListOf<Pair<String, Map<String, Any>>>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> while (nextElement()) {
                                actions.add(map())
                            }
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
    val actions: List<Map<String, Any>>,
    val produces: List<Pair<String, Map<String, Any>>>,
) {
    fun activity(template: Template) = BotActivity(
        id = id,
        capacity = int,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
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
        produces = resolveRequirements(template.produces, produces, requirePredicates = false).toSet(),
    )

    fun shortcut(template: Template) = NavigationShortcut(
        id = id,
        weight = int,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        produces = resolveRequirements(template.produces, produces, requirePredicates = false).toSet(),
    )
}

private data class Template(
    val requires: List<Pair<String, Map<String, Any>>>,
    val setup: List<Pair<String, Map<String, Any>>>,
    val actions: List<Map<String, Any>>,
    val produces: List<Pair<String, Map<String, Any>>>,
)
