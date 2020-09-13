package rs.dusk.engine.task

/**
 * Repeats a task every tick
 * Note: Not designed for general use
 */
data class RepeatTask(private val task: (Long) -> Unit) : CancelTask() {

    override fun run(tick: Long) = task.invoke(tick)

}

fun TaskExecutor.repeat(task: (Long) -> Unit) = RepeatTask(task).apply { execute(this) }