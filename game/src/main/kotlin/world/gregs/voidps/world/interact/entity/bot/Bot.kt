package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.ai.inverse
import world.gregs.voidps.engine.entity.character.clear
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.MapArea
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.Dijkstra
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.utility.get
import java.util.*

val Player.context: BotContext
    get() = get("context")

val Player.botOptions: MutableSet<SimpleBotOption<*>>
    get() = get("options")

val Player.isBot: Boolean
    get() = get("bot", false)

val Player.steps: LinkedList<Instruction>
    get() = get("steps")

val Player.patience: Double
    get() = get("patience")

val Player.impatience: Double
    get() = patience.inverse()

var Player.step: Instruction?
    get() = getOrNull("step")
    set(value) {
        if (value != null) {
            set("step", value)
        } else {
            clear("step")
        }
    }

val Player.woodcuttingDesire: Double
    get() = get("woodcuttingDesire", 0.0)

val Player.logStorageDesire: Double
    get() = get("logStorageDesire", 0.0)

fun Player.setDesire(item: String, value: Double) {
    desiredItems[item] = value
    undesiredItems.remove(item)
}

fun Player.setUndesired(item: String, value: Double) {
    undesiredItems[item] = value
    desiredItems.remove(item)
}

val Player.desiredExperience: MutableMap<Skill, Double>
    get() = get("experienceDesire")

val Player.desiredItemStorage: MutableMap<String, Double>
    get() = get("itemStorageDesire")

val Player.desiredItems: MutableMap<String, Double>
    get() = get("itemDesire")

val Player.undesiredItems: MutableMap<String, Double>
    get() = get("undesiredItems")

val Player.area: MapArea?
    get() = getOrNull("area")

fun Player.initBot() {
    this["bot"] = true
    this["context"] = BotContext(this)
    this["steps"] = LinkedList<Instruction>()
    this["options"] = mutableSetOf<SimpleBotOption<*>>()
    this["woodcuttingDesire"] = 1.0
    this["logStorageDesire"] = 1.0
    this["patience"] = 0.5
    this["itemDesire"] = mutableMapOf<String, Double>()
    this["itemStorageDesire"] = mutableMapOf<String, Double>()
    this["undesiredItems"] = mutableMapOf<String, Double>()
    this["experienceDesire"] = mutableMapOf<Skill, Double>()
}

fun Player.goTo(map: MapArea): PathResult {
    this["targetArea"] = map
    movement.waypoints.clear()
    steps.clear()
    step = null
    this["navigating"] = true
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node in map.area
        }
    }
    return get<Dijkstra>().find(this, strategy, EdgeTraversal())
}

fun Player.goTo(tile: Tile): PathResult {
    movement.waypoints.clear()
    steps.clear()
    step = null
    this["navigating"] = true
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node == tile
        }
    }
    return get<Dijkstra>().find(this, strategy, EdgeTraversal())
}