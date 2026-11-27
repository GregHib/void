package content.bot.behaviour

import content.bot.behaviour.action.ActionParser
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotLoadout
import content.bot.behaviour.condition.BotLoadoutQualified
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.loadout.GearDefinitions
import content.bot.behaviour.loadout.Loadout
import content.bot.behaviour.loadout.LoadoutDefinitions
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
    val reactive: List<BotAction>
    val produces: Set<String>
}

/**
 * Skill trained taken from [Behaviour.produces] e.g. "skill:mining" -> "mining"
 */
val Behaviour.skill: String?
    get() = produces.firstOrNull { it.startsWith("skill:") }?.removePrefix("skill:")

/**
 * Item gathered or made taken from [Behaviour.produces] e.g. "item:iron_ore" -> "iron_ore"
 */
val Behaviour.product: String?
    get() = produces.firstOrNull { it.startsWith("item:") }?.removePrefix("item:")

fun loadBehaviours(
    files: ConfigFiles,
    activities: MutableMap<String, BotActivity>,
    resolvers: MutableMap<String, MutableList<Resolver>>,
) {
    GearDefinitions.load(files.list(Settings["bots.gear", "gear.toml"]))
    LoadoutDefinitions.load(files.list(Settings["bots.loadouts", "loadouts.toml"]))
    val templates = loadTemplates(files.list(Settings["bots.templates"]))
    loadActivities(activities, templates, files.list(Settings["bots.definitions"]))
    loadSetups(resolvers, templates, files.list(Settings["bots.setups"]))
}

internal fun loadNavigationShortcuts(templatePaths: List<String>, shortcutPaths: List<String>): List<NavigationShortcut> {
    val shortcuts = mutableListOf<NavigationShortcut>()
    loadShortcuts(shortcuts, loadTemplates(templatePaths), shortcutPaths)
    return shortcuts
}

private fun loadActivities(activities: MutableMap<String, BotActivity>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot activity") {
        val fragments = mutableListOf<Fragment>()
        load(paths) { id, template, fields, _, timeout, requires, setup, actions, reactive, produces ->
            if (template != null) {
                requireNotNull(fields)
                fragments.add(Fragment(id, template, fields, 1, timeout, requires, setup, actions, reactive, produces))
            } else {
                val debug = "$id ${exception()}"
                activities[id] = BotActivity(
                    id = id,
                    timeout = timeout,
                    requires = Condition.parse(requires, debug),
                    setup = Condition.parse(setup, debug),
                    actions = ActionParser.parse(actions, debug),
                    reactive = ActionParser.parse(reactive, debug),
                    produces = produces,
                ).withLoadoutRequirement()
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            activities[fragment.id] = fragment.activity(template).withLoadoutRequirement()
        }
        activities.size
    }
}

/**
 * Activities with a loadout setup require the levels to use one of its loadouts to be available.
 */
internal fun BotActivity.withLoadoutRequirement(): BotActivity {
    val loadout = loadout ?: return this
    require(setup.count { it is BotLoadout } == 1) { "Activity '$id' can only have one loadout setup." }
    return copy(requires = (requires + BotLoadoutQualified(loadout.options.map { it.first })).sortedBy { it.priority })
}

private fun <T : Behaviour> T.noLoadouts(): T {
    require(setup.none { it is BotLoadout } && requires.none { it is BotLoadout }) { "Loadouts are only supported in activity setups, found in '$id'." }
    return this
}

private fun loadSetups(resolvers: MutableMap<String, MutableList<Resolver>>, templates: Map<String, Template>, paths: List<String>) {
    timedLoad("bot setup") {
        val fragments = mutableListOf<Fragment>()
        load(paths) { id, template, fields, weight, timeout, requires, setup, actions, reactive, produces ->
            if (template != null) {
                requireNotNull(fields)
                fragments.add(Fragment(id, template, fields, weight, timeout, requires, setup, actions, reactive, produces))
            } else {
                val debug = "$id ${exception()}"
                val resolver = Resolver(
                    id = id,
                    weight = weight,
                    timeout = timeout,
                    requires = Condition.parse(requires, debug),
                    setup = Condition.parse(setup, debug),
                    actions = ActionParser.parse(actions, debug),
                    reactive = ActionParser.parse(reactive, debug),
                    produces = produces,
                ).noLoadouts()
                for (key in produces) {
                    resolvers.getOrPut(key) { mutableListOf() }.add(resolver)
                }
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            val resolver = fragment.resolver(template).noLoadouts()
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
        load(paths) { id, template, fields, weight, timeout, requires, setup, actions, reactive, produces ->
            if (template != null) {
                requireNotNull(fields) { "No fields found for $id ${exception()}" }
                fragments.add(Fragment(id, template, fields, weight, timeout, requires, setup, actions, reactive, produces))
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
                        reactive = ActionParser.parse(reactive, debug),
                        produces = produces,
                    ).noLoadouts(),
                )
            }
        }
        for (fragment in fragments) {
            val template = templates[fragment.template] ?: error("Unable to find template '${fragment.template}' for ${fragment.id}.")
            shortcuts.add(fragment.shortcut(template).noLoadouts())
        }
        shortcuts.size
    }
}

private fun load(paths: List<String>, block: ConfigReader.(String, String?, Map<String, Any>?, Int, Int, List<Pair<String, List<Map<String, Any>>>>, List<Pair<String, List<Map<String, Any>>>>, List<Pair<String, Map<String, Any>>>, List<Pair<String, Map<String, Any>>>, Set<String>) -> Unit) {
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
                val reactive = mutableListOf<Pair<String, Map<String, Any>>>()
                val produces = mutableSetOf<String>()
                while (nextPair()) {
                    when (val key = key()) {
                        "template" -> template = string()
                        "requires" -> requirements(requires)
                        "setup" -> requirements(setup)
                        "actions" -> actions(actions)
                        "reactive" -> actions(reactive)
                        "produces" -> produces(produces)
                        "weight" -> value = int()
                        // Deprecated: capacity is now set per area with `bot_capacity`
                        "capacity" -> int()
                        "timeout" -> timeout = int()
                        "fields" -> fields = map()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                if (fields != null && template == null) {
                    error("Found fields but no template for $id in ${exception()}")
                }
                block.invoke(this, id, template, fields, value, timeout, requires, setup, actions, reactive, produces)
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
                    val reactive = mutableListOf<Pair<String, Map<String, Any>>>()
                    val produces = mutableSetOf<String>()
                    var switchLoadouts: Map<String, Any> = emptyMap()
                    var startingLoadout: String? = null
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements(requires)
                            "setup" -> requirements(setup)
                            "actions" -> actions(actions)
                            "reactive" -> actions(reactive)
                            "produces" -> produces(produces)
                            "switch_loadouts" -> switchLoadouts = map()
                            "starting_loadout" -> startingLoadout = string()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    templates[id] = Template(requires, setup, actions, reactive, produces, switchLoadouts, startingLoadout)
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
    val reactive: List<Pair<String, Map<String, Any>>>,
    val produces: Set<String>,
) {
    fun activity(template: Template) = BotActivity(
        id = id,
        timeout = timeout,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = resolveActions(template.actions, actions),
        reactive = resolveActions(template.reactive, reactive),
        produces = resolve(template.produces) + produces,
        switchLoadouts = resolveSwitchLoadouts(template),
        startingLoadout = template.startingLoadout,
    )

    @Suppress("UNCHECKED_CAST")
    private fun resolveSwitchLoadouts(template: Template): Map<String, Loadout> {
        val out = LinkedHashMap<String, Loadout>(template.switchLoadouts.size)
        val debug = "$id template ${this.template} switch_loadouts"
        for ((name, raw) in template.switchLoadouts) {
            out[name] = when (raw) {
                is String -> LoadoutDefinitions.getOrNull(raw) ?: error("Unknown loadout '$raw' for '$name' in $debug.")
                is Map<*, *> -> {
                    val resolved = resolve(raw as Map<String, Any>, "switch_loadouts.$name")
                    require(resolved["equipment"] is Map<*, *>) { "Loadout '$name' missing 'equipment' map in $debug." }
                    Loadout.parse(name, resolved, GearDefinitions::getOrNull).also { it.validate() }
                }
                else -> error("Loadout '$name' must be a map or loadout id in $debug, got $raw.")
            }
        }
        val starting = template.startingLoadout
        require(starting == null || out.containsKey(starting)) { "Starting loadout '$starting' isn't one of the switch_loadouts in $debug." }
        return out
    }

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
            combinedList.add(type to resolve(map, type))
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
        reactive = resolveActions(template.reactive, reactive),
        produces = resolve(template.produces) + produces,
    )

    fun shortcut(template: Template) = NavigationShortcut(
        id = id,
        weight = int,
        timeout = timeout,
        requires = resolveRequirements(template.requires, requires),
        setup = resolveRequirements(template.setup, setup),
        actions = resolveActions(template.actions, actions),
        reactive = resolveActions(template.reactive, reactive),
        produces = resolve(template.produces) + produces,
    )
}

private data class Template(
    val requires: List<Pair<String, List<Map<String, Any>>>>,
    val setup: List<Pair<String, List<Map<String, Any>>>>,
    val actions: List<Pair<String, Map<String, Any>>>,
    val reactive: List<Pair<String, Map<String, Any>>>,
    val produces: Set<String>,
    val switchLoadouts: Map<String, Any> = emptyMap(),
    val startingLoadout: String? = null,
)
