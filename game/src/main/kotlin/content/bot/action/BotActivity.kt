package content.bot.action

import content.bot.req.CloneRequirement
import content.bot.req.RequiresInvSpace
import content.bot.req.RequiresCarriedItem
import content.bot.req.MandatoryRequirement
import content.bot.req.Requirement
import content.bot.req.RequiresEquippedItem
import content.bot.req.RequiresLocation
import content.bot.req.RequiresOwnedItem
import content.bot.req.RequiresSkill
import content.bot.req.RequiresVariable
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.timedLoad

data class BotActivity(
    override val id: String,
    val capacity: Int,
    override val requirements: List<Requirement> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
) : Behaviour

fun loadActivities(paths: List<String>): Map<String, BotActivity> {
    val activities = mutableMapOf<String, BotActivity>()
    timedLoad("bot activity") {
        val clones = mutableMapOf<String, String>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var capacity = 0
                    var type = "activity"
                    var weight = 0
                    var actions: List<BotAction> = emptyList()
                    var requirements: List<Requirement> = emptyList()
                    while (nextPair()) {
                        when (val key = key()) {
                            "requires" -> requirements = requirements()
                            "plan" -> actions = actions()
                            "produces" -> produces()
                            "capacity" -> capacity = int()
                            "type" -> type = string()
                            "weight" -> weight = int()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    val clone = requirements.filterIsInstance<CloneRequirement>().firstOrNull()
                    if (clone != null) {
                        clones[id] = clone.id
                    }
                    activities[id] = BotActivity(id, capacity, requirements, plan = actions)
                }
            }
        }
        for (activity in activities.values) {
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
                val requirements = activity.requirements as MutableList<Requirement>
                requirements.removeIf { it is CloneRequirement && it.id == cloneId }
                requirements.addAll(clone.requirements)
            }
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

private fun ConfigReader.requirements(): List<Requirement> {
    val list = mutableListOf<Requirement>()
    while (nextElement()) {
        var type = ""
        var id = ""
        var value: Any? = null
        var min = 1
        var max = 1
        while (nextEntry()) {
            when (val key = key()) {
                "skill", "carries", "equips", "owns", "variable", "clone", "location" -> {
                    type = key
                    id = string()
                }
                "amount" -> {
                    min = int()
                    max = min
                }
                "min" -> min = int()
                "max" -> max = int()
                "inventory_space" -> {
                    type = key
                    min = int()
                }
                "value" -> value = value()
            }
        }
        val requirement = when (type) {
            "skill" -> RequiresSkill(id, min, max)
            "carries" -> RequiresCarriedItem(id, min)
            "owns" -> RequiresOwnedItem(id, min)
            "equips" -> RequiresEquippedItem(id, min)
            "variable" -> RequiresVariable(id, value)
            "clone" -> CloneRequirement(id)
            "inventory_space" -> RequiresInvSpace(min)
            "location" -> RequiresLocation(id)
            "holds" -> throw IllegalArgumentException("Unknown requirement type 'holds'; did you mean 'carries' or 'equips'? ${exception()}.")
            else -> throw IllegalArgumentException("Unknown requirement type: $type ${exception()}")
        }
        list.add(requirement)
    }
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
        var radius = 0
        while (nextEntry()) {
            when (val key = key()) {
                "go_to", "wait_for", "interface", "npc", "object", "clone" -> {
                    type = key
                    id = string()
                }
                "wait" -> {
                    type = key
                    ticks = int()
                }
                "radius" -> radius = int()
                "target", "id" -> id = string()
                "retry_ticks" -> retryTicks = int()
                "retry_max" -> retryMax = int()
                "option" -> option = string()
                "timeout" -> timeout = int()
                else -> throw IllegalArgumentException("Unknown action key: $key ${exception()}")
            }
        }
        val action = when (type) {
            "go_to" -> BotAction.GoTo(id)
            "wait" -> BotAction.Wait(ticks)
            "npc" -> BotAction.InteractNpc(id, option, retryTicks, retryMax, radius)
            "object" -> BotAction.InteractObject(id, option, retryTicks, retryMax, radius)
            "interface" -> BotAction.InterfaceOption(option, id)
            "clone" -> BotAction.Clone(id)
            "wait_for" -> when (id) {
                "full_inventory" -> BotAction.WaitFullInventory(timeout)
                else -> throw IllegalArgumentException("Unknown wait_for action: $id ${exception()}")
            }
            else -> throw IllegalArgumentException("Unknown action type: $type ${exception()}")
        }
        list.add(action)
    }
    return list
}