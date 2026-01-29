package content.bot

import content.bot.action.BehaviourFrame
import content.bot.action.BehaviourState
import content.bot.action.BotAction
import content.bot.action.BotActivity
import content.bot.action.Reason
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Instruction
import java.util.Stack

data class Bot(val player: Player) : Character by player {
    var step: Instruction? = null
    val blocked: MutableSet<String> = mutableSetOf()
    var previous: BotActivity? = null
    val frames = Stack<BehaviourFrame>()
    val available = mutableSetOf<String>()

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

    override fun toString(): String {
        return "BOT ${player.accountName}"
    }
}
