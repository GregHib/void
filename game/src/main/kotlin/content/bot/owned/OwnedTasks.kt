package content.bot.owned

import content.bot.BotManager
import content.bot.behaviour.activity.BotActivity
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
)

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
        val map = linkedMapOf<String, OwnedTask>()
        for ((index, task) in found.withIndex()) {
            map[task.id] = task.copy(index = index)
        }
        built = map
        return map
    }

    private fun task(activity: BotActivity): OwnedTask? {
        val id = activity.id
        if (id in hidden || id.startsWith("owned")) {
            return null
        }
        val skill = activity.skill ?: return null
        val product = activity.product ?: return null
        if (skill in COMBAT) {
            return null
        }
        val level = activity.requires.filterIsInstance<BotSkillLevel>().firstOrNull { it.skill.name.equals(skill, ignoreCase = true) }?.min ?: 1
        val members = activity.requires.any { it is BotMembers && it.members }
        val area = activity.area?.id
        val location = places[id] ?: area?.let { folders[it] }?.let { folder -> locations[folder] ?: folder.split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) } } ?: ""
        val item = ItemDefinitions.get(product).name.ifBlank { product.replace('_', ' ') }
        val label = labels[id] ?: "${verbs[skill] ?: skill.replaceFirstChar(Char::uppercase)} ${item.lowercase()}"
        return OwnedTask(id, label, id, 0, skill, level, location, members)
    }

    companion object {
        /** The order skills are listed in, those not here after, alphabetically. */
        val SKILLS = listOf("woodcutting", "mining", "fishing", "cooking", "smithing", "fletching", "crafting", "thieving", "firemaking", "herblore", "runecrafting", "hunter", "farming", "agility")
        private val COMBAT = setOf("attack", "strength", "defence", "constitution", "ranged", "magic", "prayer", "summoning", "slayer")
    }
}
