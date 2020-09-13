package rs.dusk.engine.task

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.entity.character.player.Player

/**
 * Executes a task after [executionTick] ticks, cancelling if player logs out
 */
data class PlayerTask(val player: Player, val executionTick: Long, private val task: PlayerTask.(Long) -> Unit) : CancelTask() {

    override fun isTimeToRun(tick: Long) = super.isTimeToRun(tick) && isTimeUp(tick)

    override fun isTimeToRemove(tick: Long) = super.isTimeToRemove(tick) || isTimeUp(tick) || player.action.type == ActionType.Logout

    private fun isTimeUp(tick: Long) = tick >= executionTick

    override fun run(tick: Long) = task.invoke(this, tick)
}

fun TaskExecutor.delay(player: Player, ticks: Int = 0, task: PlayerTask.(Long) -> Unit)
        = PlayerTask(player, tick + ticks, task).apply { execute(this) }