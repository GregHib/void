package content.bot

import content.bot.behaviour.Behaviour
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotGoToNearest
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.perception.BotCombatContext
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.Stack

data class Bot(val player: Player) : Character by player {
    val blocked: MutableSet<String> = mutableSetOf()
    var previous: BotActivity? = null
    val frames = Stack<BehaviourFrame>()
    val available = mutableSetOf<String>()
    var evaluate = mutableSetOf<String>()
    var combatContext: BotCombatContext? = null

    /**
     * Forces the manager to always (re)assign this activity id instead of picking from [available] or [previous].
     *
     * Used by bots that are designed for a single role (e.g. PvP clan-war tiers) where the normal pick/reuse
     * path would drift to another activity after a hard-fail, timeout, or death. Read by:
     * - [content.bot.BotManager.assignRandom]: skips random selection, always assigns the pinned id.
     * - [content.bot.BotManager.start]: if a non-area setup requirement fails, invokes [refresh] instead of
     *   spawning a resolver frame (bots shouldn't wander off to "fetch" missing kit).
     * - [content.bot.BotManager.handleFail]: soft-fail on the pinned activity does not blacklist it.
     */
    var pinned: String? = null

    fun noTask() = frames.isEmpty()

    /**
     * The behaviour being worked on, the bottom frame as resolvers (banking, buying tools) sit on top of it
     */
    val activity: Behaviour?
        get() = frames.firstOrNull()?.behaviour

    /**
     * Area of the top-most running go to action, resolvers sit above their activity so a trip to the bank is found
     * before the activity's own area. Null if not walking anywhere or already there.
     */
    fun destination(): AreaDefinition? {
        for (index in frames.indices.reversed()) {
            val frame = frames[index]
            if (frame.state == BehaviourState.Pending || frame.completed()) {
                continue
            }
            val area = when (val action = frame.action()) {
                is BotGoTo -> Areas.getOrNull(action.target)
                is BotGoToNearest -> Areas.tagged(action.tag).minByOrNull { it.area.first().distanceTo(tile) }
                else -> null
            } ?: continue
            return if (tile in area.area) null else area
        }
        return null
    }

    internal fun action(): BotAction = frames.peek().action()

    internal fun frame(): BehaviourFrame = frames.peek()

    internal fun reset() {
        frames.clear()
    }

    internal fun queue(frame: BehaviourFrame) {
        frames.add(frame)
    }

    fun stop() {
        if (noTask()) {
            return
        }
        frame().state = BehaviourState.Failed(Reason.Cancelled)
    }

    override fun toString(): String = "BOT ${player.accountName}"
}

val Player.isBot: Boolean
    get() = contains("bot")

val Player.bot: Bot
    get() = get("bot")!!
