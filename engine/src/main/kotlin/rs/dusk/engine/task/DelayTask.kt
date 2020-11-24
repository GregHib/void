package rs.dusk.engine.task

/**
 * Executes a task after [executionTick] ticks
 */
data class DelayTask(val executionTick: Long, private val task: (Long) -> Unit) : CancelTask() {

    override fun isTimeToRun(tick: Long) = super.isTimeToRun(tick) && isTimeUp(tick)

    override fun isTimeToRemove(tick: Long) = super.isTimeToRemove(tick) || isTimeUp(tick)

    private fun isTimeUp(tick: Long) = tick >= executionTick

    override fun run(tick: Long) = task.invoke(tick)
}

fun TaskExecutor.delay(ticks: Int, task: (Long) -> Unit)
        = DelayTask(tick + ticks, task).apply { execute(this) }

fun TaskExecutor.delay(ticks: Long = 0L, task: (Long) -> Unit)
        = DelayTask(tick + ticks, task).apply { execute(this) }