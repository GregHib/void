package content.bot.behaviour.condition

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.epochSeconds

data class BotClockRemaining(val id: String, val min: Int? = null, val max: Int? = null, val seconds: Boolean = false) : Condition(1) {
    override fun keys() = setOf("clock:$id")
    override fun events() = setOf("clock")
    override fun check(player: Player): Boolean {
        val remaining = player.remaining(id, if (seconds) epochSeconds() else GameLoop.Companion.tick)
        return inRange(remaining, min, max)
    }
}