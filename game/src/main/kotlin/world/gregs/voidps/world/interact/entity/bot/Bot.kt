package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.ai.inverse
import world.gregs.voidps.engine.entity.character.clear
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.getOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
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

fun Player.initBot() {
    this["bot"] = true
    this["context"] = BotContext(this)
    this["steps"] = LinkedList<Instruction>()
    this["options"] = mutableSetOf<SimpleBotOption<*>>()
    this["woodcuttingDesire"] = 1.0
    this["patience"] = 0.5
}

fun Player.goTo(area: Area): PathResult {
    movement.waypoints.clear()
    steps.clear()
    step = null
    this["navigating"] = true
    val strategy = object : NodeTargetStrategy() {
        override fun reached(node: Any): Boolean {
            return node is Tile && node in area
        }
    }
    return get<Dijkstra>().find(this, strategy, EdgeTraversal())
}