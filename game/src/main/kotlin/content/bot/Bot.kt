package content.bot

import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.Reason
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.perception.BotCombatContext
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

    /**
     * Re-applies tier-specific state (skills, equipment, inventory) when the pinned activity's setup
     * requirement fails. Called from [content.bot.BotManager.start] instead of running a resolver.
     */
    var refresh: (() -> Unit)? = null

    fun noTask() = frames.isEmpty()

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
