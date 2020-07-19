package rs.dusk.engine.task

interface Task {
    fun isTimeToRun(tick: Long): Boolean

    fun isTimeToRemove(tick: Long): Boolean

    fun run(tick: Long)

    fun cancel()
}