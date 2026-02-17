package content.bot.behaviour

import content.bot.behaviour.action.ActionParser
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.navigation.NavigationShortcut
import content.bot.behaviour.setup.Resolver
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

interface Behaviour {
    val id: String
    val timeout: Int
    val requires: List<Condition>
    val setup: List<Condition>
    val actions: List<BotAction>
    val produces: Set<String>
}

fun loadBehaviours(
    files: ConfigFiles,
    activities: MutableMap<String, BotActivity>,
    resolvers: MutableMap<String, MutableList<Resolver>>,
) {
    val templates = loadTemplates(files.list(Settings["bots.templates"]))
    loadActivities(activities, templates, files.list(Settings["bots.definitions"]))
    loadSetups(resolvers, templates, files.list(Settings["bots.setups"]))
}

fun loadGraph(files: ConfigFiles): NavigationGraph {
    val templates = loadTemplates(files.list(Settings["bots.templates"]))
    val shortcuts = mutableListOf<NavigationShortcut>()
    loadShortcuts(shortcuts, templates, files.list(Settings["bots.shortcuts"]))
    return NavigationGraph.loadGraph(files.list(Settings["bots.nav.definitions"]), shortcuts)
}

private fun loadActivities(activities: MutableMap<String, BotActivity>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot activity") {
        val fragments = mutableListOf<Fragment>()
        load(paths) { id, template, fields, capacity, timeout, requires, setup, actions, produces ->
            if (template != null) {
                requireNotNull(fields)
                fragments.add(Fragment(id, template, fields, capacity, timeout, requires, setup, actions, produces))
            } else {
                val debug = "$id ${exception()}"
                activities[id] = BotActivity(
                    id = id,
                    capacity = capacity,
                    timeout = timeout,
                    requires = Condition.parse(requires, debug),
                    setup = Condition.parse(setup, debug),
                    actions = ActionParser.parse(actions, debug),
                    produces = produces,
                )
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            activities[fragment.id] = fragment.activity(template)
        }
        activities.size
    }
}

private fun loadSetups(resolvers: MutableMap<String, MutableList<Resolver>>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot setup") {
        val fragments = mutableListOf<Fragment>()
        load(paths) { id, template, fields, weight, timeout, requires, setup, actions, produces ->
            if (template != null) {
                requireNotNull(fields)
                fragments.add(Fragment(id, template, fields, weight, timeout, requires, setup, actions, produces))
            } else {
                val debug = "$id ${exception()}"
                val resolver = Resolver(
                    id = id,
                    weight = weight,
                    timeout = timeout,
                    requires = Condition.parse(requires, debug),
                    setup = Condition.parse(setup, debug),
                    actions = ActionParser.parse(actions, debug),
                    produces = produces,
                )
                for (key in produces) {
                    resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
                }
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            val resolver = fragment.resolver(template)
            for (key in resolver.produces) {
                resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
            }
        }
        resolvers.size
    }
}

private fun loadShortcuts(shortcuts: MutableList<NavigationShortcut>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot shortcut") {
        val fragments = mutableListOf<Fragment>()
        load(paths) { id, template, fields, weight, timeout, requires, setup, actions, produces ->
            if (template != null) {
                requireNotNull(fields) { "No fields found for $id ${exception()}" }
                fragments.add(Fragment(id, template, fields, weight, timeout, requires, setup, actions, produces))
            } else {
                val debug = "$id ${exception()}"
                shortcuts.add(
                    NavigationShortcut(
                        id = id,
                        weight = weight,
                        timeout = timeout,
                        requires = Condition.parse(requires, debug),
                        setup = Condition.parse(setup, debug),
                        actions = ActionParser.parse(actions, debug),
                        produces = produces,
                    ),
                )
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            shortcuts.add(fragment.shortcut(template))
        }
        shortcuts.size
    }
}

private fun load(paths: List<String>, block: ConfigReader.(String, String?, Map<String, Any>?, Int, Int, List<Pair<String, List<Map<String, Any>>>>, List<Pair<String, List<Map<String, Any>>>>, List<Pair<String, Map<String, Any>>>, Set<String>) -> Unit) {
    for (path in paths) {
        Config.fileReader(path) {
            while (nextSection()) {
                val id = section()
                var template: String? = null
                var fields: Map<String, Any>? = null
                var value = 1
                var timeout = TimeUnit.MINUTES.toTicks(1)
                val requires = mutableListOf<Pair<String, List<Map<String, Any>>>>()
                val setup = mutableListOf<Pair<String, List<Map<String, Any>>>>()
                val actions = mutableListOf<Pair<String, Map<String, Any>>>()
                val produces = mutableSetOf<String>()
                while (nextPair()) {
                    when (val key = key()) {
                        "template" -> template = string()
                        "requires" -> requirements(requires)
                        "setup" -> requirements(setup)
                        "actions" -> actions(actions)
                        "produces" -> produces(produces)
                        "weight", "capacity" -> value = int()
                        "timeout" -> timeout = int()
                        "fields" -> fields = map()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                if (fields != null && template == null) {
                    error("Found fields but no template for $id in ${exception()}")
                }
                block.invoke(this, id, template, fields, value, timeout, requires, setup, actions, produces)
            }
        }
    }
}

private fun loadTemplates(paths: List<String>): Map<String, Template> {
    val templates = mutableMapOf<String, Template>()
    timedLoad("bot template") {
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    val requires = mutableListOf<Pair<String, List<Map<String, Any>>>>()
                    val setup = mutableListOf<Pair<String, List<Map<String, Any>>>>()
                    val actions = mutableListOf<Pair<String, Map<String, Any>>>()
                    val produces = mutableSetOf<String>()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "produces" -> produces(produces)
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

internal fun ConfigReader.requirements(requires: MutableList<Pair<String, List<Map<String, Any>>>>) {
    while (nextElement()) {
        while (nextEntry()) {
            val key = key()
            if (peek == '[') {
                val list = ObjectArrayList<Map<String, Any>>()
                while (nextElement()) {
                    list.add(map())
                }
                requires.add(key to list)
            } else {
                requires.add(key to listOf(map()))
            }
        }
    }
}

internal fun ConfigReader.produces(requires: MutableSet<String>) {
    while (nextElement()) {
        while (nextEntry()) {
            val key = key()
            val value = string()
            requires.add("$key:$value")
        }
    }
}

internal fun ConfigReader.actions(list: MutableList<Pair<String, Map<String, Any>>>) {
    while (nextElement()) {
        while (nextEntry()) {
            val type = key()
            val map = map()
            list.add(type to map)
        }
    }
}

private data class Fragment(
    val id: String,
    val template: String,
    val fields: Map<String, Any>,
    val int: Int,
    val timeout: Int,
    val requires: List<Pair<String, List<Map<String, Any>>>>,
    val setup: List<Pair<String, List<Map<String, Any>>>>,
    val actions: List<Pair<String, Map<String, Any>>>,
    val produces: Set<String>,
) {
    fun activity(template: Template) = BotActivity(
        id = id,
        capacity = int,
        timeout = timeout,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = resolveActions(template.actions, actions),
        produces = resolve(template.produces) + produces,
    )

    private fun resolve(set: Set<String>) = set.map { value ->
        if (value.contains('$')) {
            val ref = value.reference()
            val name = ref.trim('$', '{', '}')
            val replacement = fields[name] as? String ?: error("No field found for behaviour=$id ref=$ref")
            value.replace(ref, replacement)
        } else {
            value
        }
    }.toSet()

    private fun resolveRequirements(templated: List<Pair<String, List<Map<String, Any>>>>, original: List<Pair<String, List<Map<String, Any>>>>): List<Condition> {
        val combinedList = mutableListOf<Pair<String, List<Map<String, Any>>>>()
        combinedList.addAll(original)
        for ((type, list) in templated) {
            val resolved = list.map { map -> resolve(map, type) }
            if (resolved.isNotEmpty()) {
                combinedList.add(type to resolved)
            }
        }
        if (combinedList.isEmpty()) {
            return emptyList()
        }
        return Condition.parse(combinedList, "$id template $template")
    }

    private fun resolveActions(templated: List<Pair<String, Map<String, Any>>>, original: List<Pair<String, Map<String, Any>>>): List<BotAction> {
        val combinedList = mutableListOf<Pair<String, Map<String, Any>>>()
        combinedList.addAll(original)
        for ((type, map) in templated) {
            val resolved = resolve(map, type)
            if (resolved.isNotEmpty()) {
                combinedList.add(type to resolved)
            }
        }
        if (combinedList.isEmpty()) {
            return emptyList()
        }
        return ActionParser.parse(combinedList, "$id template $template")
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolve(map: Map<String, Any>, type: String): Map<String, Any> = map.mapValues { (key, value) ->
        if (value is String && value.contains('$')) {
            val ref = value.reference()
            val name = ref.trim('$', '{', '}')
            val replacement = fields[name] ?: error("No field found for behaviour=$id type=$type key=$key ref=$ref")
            if (replacement is String) value.replace(ref, replacement) else replacement
        } else if (value is Map<*, *>) {
            resolve(value as Map<String, Any>, type)
        } else if (value is List<*>) {
            resolve(value as List<Any>, type)
        } else {
            value
        }
    }.toMap()

    @Suppress("UNCHECKED_CAST")
    private fun resolve(value: List<Any>, type: String): List<Any> = value.map { element ->
        when (element) {
            is Map<*, *> -> resolve(element as Map<String, Any>, type)
            is List<*> -> resolve(element as List<Any>, type)
            else -> element
        }
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
        timeout = timeout,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = resolveActions(template.actions, actions),
        produces = resolve(template.produces) + produces,
    )

    fun shortcut(template: Template) = NavigationShortcut(
        id = id,
        weight = int,
        timeout = timeout,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = resolveActions(template.actions, actions),
        produces = resolve(template.produces) + produces,
    )
}

private data class Template(
    val requires: List<Pair<String, List<Map<String, Any>>>>,
    val setup: List<Pair<String, List<Map<String, Any>>>>,
    val actions: List<Pair<String, Map<String, Any>>>,
    val produces: Set<String>,
)
