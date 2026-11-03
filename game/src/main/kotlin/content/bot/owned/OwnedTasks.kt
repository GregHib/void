package content.bot.owned

import content.bot.BotManager
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotBankSetup
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotMembers
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.product
import content.bot.behaviour.skill
import world.gregs.config.Config
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

/**
 * A task an owner can give a bot: the world-bot activity it maps to (its [id]), the skill, level and place the
 * plugin groups it by, and a label. [index] is the position in the catalogue, which the client variables carry.
 * [patches] are the activities that do the same thing at the same level in the same place (the four lots of
 * trees round Lumbridge), [activity] among them: the order picks the least used.
 */
data class OwnedTask(
    val id: String,
    val label: String,
    val activity: String,
    val index: Int,
    val skill: String,
    val level: Int,
    val location: String,
    val members: Boolean,
    val patches: List<String> = listOf(activity),
    /** What the bot fetches from the owner's bank for it: each a choice of item ids (any hatchet) and how many. */
    val needs: List<Need> = emptyList(),
) {
    data class Need(val ids: List<Int>, val amount: Int)

    /** [needs] as the plugin reads them: `id/id:amount,id:amount`. */
    fun encodeNeeds(): String = needs.joinToString(",") { "${it.ids.joinToString("/")}:${it.amount}" }
}

/**
 * The catalogue of tasks owned bots can be given: every world-bot activity that ends with an item (a product and a
 * skill in its `produces`, the skill not a combat one), found when first asked for so that an activity added
 * anywhere is a task without a line of data. `owned.tasks.toml` only tunes it: `[locations]` names the area
 * folders, `[verbs]` the word a skill's labels start with, and a section named after an activity gives it a
 * `label`, a `location` or `hidden = true`. Order: skill (the usual order), location, level, label.
 */
class OwnedTasks {
    private val labels = mutableMapOf<String, String>()
    private val places = mutableMapOf<String, String>()
    private val hidden = mutableSetOf<String>()
    private val locations = mutableMapOf<String, String>()
    private val verbs = mutableMapOf<String, String>()

    /** Area id to the folder its file is in: `draynor_north_trees` to `draynor`. */
    private val folders = mutableMapOf<String, String>()
    private var built: Map<String, OwnedTask>? = null

    val all: List<OwnedTask>
        get() = catalogue().values.toList()

    val ids: Set<String>
        get() = catalogue().keys

    fun get(id: String): OwnedTask? = catalogue()[id]

    /** Forgets the built catalogue, to be found again from the activities now loaded. */
    fun reset() {
        built = null
    }

    fun load(files: ConfigFiles): OwnedTasks {
        timedLoad("owned bot task") {
            labels.clear()
            places.clear()
            hidden.clear()
            locations.clear()
            verbs.clear()
            folders.clear()
            built = null
            for (path in files.list("areas.toml")) {
                val folder = path.replace('\\', '/').substringBeforeLast('/').substringAfterLast('/')
                Config.fileReader(path) {
                    while (nextSection()) {
                        folders[section()] = folder
                        while (nextPair()) {
                            key()
                            value()
                        }
                    }
                }
            }
            for (path in files.list("tasks.toml")) {
                if (!path.endsWith("owned.tasks.toml")) {
                    continue
                }
                Config.fileReader(path) {
                    while (nextSection()) {
                        val id = section()
                        while (nextPair()) {
                            val key = key()
                            when (id) {
                                "locations" -> locations[key] = string()
                                "verbs" -> verbs[key] = string()
                                else -> when (key) {
                                    "label" -> labels[id] = string()
                                    "location" -> places[id] = string()
                                    "hidden" -> if (boolean()) hidden.add(id)
                                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                                }
                            }
                        }
                    }
                }
            }
            labels.size + locations.size
        }
        return this
    }

    private fun catalogue(): Map<String, OwnedTask> {
        built?.let { return it }
        val manager: BotManager = get()
        val found = mutableListOf<OwnedTask>()
        for (id in manager.activityNames) {
            val activity = manager.activity(id) ?: continue
            val task = task(activity) ?: continue
            found.add(task)
        }
        found.sortWith(compareBy({ SKILLS.indexOf(it.skill).let { i -> if (i == -1) SKILLS.size else i } }, { it.skill }, { it.location }, { it.level }, { it.label }))
        // several patches of the same thing in one place (four lots of trees round Lumbridge) are one task, which
        // remembers them all so the order can spread the bots over them
        val patches = linkedMapOf<List<Any>, MutableList<String>>()
        val first = linkedMapOf<List<Any>, OwnedTask>()
        for (task in found) {
            val key = listOf(task.skill, task.location, task.level, task.label)
            patches.getOrPut(key) { mutableListOf() }.add(task.id)
            first.putIfAbsent(key, task)
        }
        val map = linkedMapOf<String, OwnedTask>()
        for ((key, task) in first) {
            map[task.id] = task.copy(index = map.size, patches = patches[key]!!.toList())
        }
        built = map
        return map
    }

    private fun task(activity: BotActivity): OwnedTask? {
        val id = activity.id
        if (id in hidden || id.startsWith("owned")) {
            return null
        }
        // the last skill listed is the end product's: chopping then fletching is a fletching task; an item with no
        // skill at all (picking flax) is gathering
        val product = activity.product ?: return null
        val skill = activity.produces.lastOrNull { it.startsWith("skill:") }?.removePrefix("skill:") ?: GATHERING
        if (skill in COMBAT) {
            return null
        }
        val level = activity.requires.filterIsInstance<BotSkillLevel>().firstOrNull { it.skill.name.equals(skill, ignoreCase = true) }?.min ?: 1
        val members = activity.requires.any { it is BotMembers && it.members }
        val area = activity.area?.id
        val location = places[id] ?: area?.let { folders[it] }?.let { folder -> locations[folder] ?: folder.split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) } } ?: ""
        val label = labels[id] ?: label(id, skill, product, area)
        return OwnedTask(id, label, id, 0, skill, level, location, members, needs = needs(activity))
    }

    /** The items an activity's setup asks for, carried, worn or banked: a choice of ids each, and how many. */
    private fun needs(activity: BotActivity): List<OwnedTask.Need> {
        val items = mutableListOf<BotItem>()
        for (condition in activity.setup) {
            when (condition) {
                is BotInventorySetup -> items.addAll(condition.items)
                is BotEquipmentSetup -> items.addAll(condition.items.values)
                is BotBankSetup -> items.addAll(condition.items)
                else -> {}
            }
        }
        val needs = linkedMapOf<List<Int>, Int>()
        for (item in items) {
            if (item.ids.contains("empty")) {
                continue
            }
            val ids = item.ids.mapNotNull { ItemDefinitions.getOrNull(it)?.id }
            if (ids.isEmpty()) {
                continue
            }
            needs[ids] = maxOf(needs[ids] ?: 0, item.min ?: 1)
        }
        return needs.map { (ids, amount) -> OwnedTask.Need(ids, amount) }
    }

    /**
     * "<verb> <product>", the product by its item's name ("Chop oak logs", fishing without the "raw"); for a product
     * that isn't an item, the activity's own name without its area folder ("lumbridge_cook_chicken": "Cook chicken").
     */
    private fun label(id: String, skill: String, product: String, area: String?): String {
        val verb = verbs[skill] ?: skill.replaceFirstChar(Char::uppercase)
        val name = ItemDefinitions.getOrNull(product)?.name?.takeIf { it.isNotBlank() && it != "null" }
        if (name == null) {
            val folder = area?.let { folders[it] }
            val words = (if (folder != null && id.startsWith("${folder}_")) id.removePrefix("${folder}_") else id).split('_').filter { it.isNotBlank() }
            return words.joinToString(" ").replaceFirstChar(Char::uppercase)
        }
        var item = name.lowercase()
        if (skill == "fishing") {
            item = item.removePrefix("raw ")
        }
        return "$verb $item"
    }

    companion object {
        /** The order skills are listed in, those not here after, alphabetically. */
        val SKILLS = listOf("woodcutting", "mining", "fishing", "cooking", "smithing", "fletching", "crafting", "thieving", "firemaking", "herblore", "runecrafting", "hunter", "farming", "agility", GATHERING)

        /** The "skill" of an activity that ends with an item but trains none: picking flax. */
        const val GATHERING = "gathering"
        private val COMBAT = setOf("attack", "strength", "defence", "constitution", "ranged", "magic", "prayer", "summoning", "slayer")
    }
}
