package content.bot.owned

import world.gregs.config.Config
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.timedLoad

/**
 * A task an owner can give a bot: a label for the plugin and the world-bot activity it maps to.
 * [index] is the position in `owned.tasks.toml`, which is what the client variables carry.
 */
data class OwnedTask(val id: String, val label: String, val activity: String, val index: Int)

/**
 * The catalogue of tasks owned bots can be given, loaded from `owned.tasks.toml`.
 */
class OwnedTasks {
    private val byId = linkedMapOf<String, OwnedTask>()

    val all: List<OwnedTask>
        get() = byId.values.toList()

    val ids: Set<String>
        get() = byId.keys

    fun get(id: String): OwnedTask? = byId[id]

    fun load(files: ConfigFiles): OwnedTasks {
        timedLoad("owned bot task") {
            byId.clear()
            for (path in files.list("tasks.toml")) {
                if (!path.endsWith("owned.tasks.toml")) {
                    continue
                }
                Config.fileReader(path) {
                    while (nextSection()) {
                        val id = section()
                        var label = id
                        var activity = ""
                        while (nextPair()) {
                            when (val key = key()) {
                                "label" -> label = string()
                                "activity" -> activity = string()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        require(activity.isNotBlank()) { "Owned task '$id' has no activity ${exception()}" }
                        byId[id] = OwnedTask(id, label, activity, byId.size)
                    }
                }
            }
            byId.size
        }
        return this
    }
}
