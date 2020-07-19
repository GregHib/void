package rs.dusk.engine.task

abstract class CancelTask : Task {

    private var cancelled = false

    override fun isTimeToRun(tick: Long) = !cancelled

    override fun isTimeToRemove(tick: Long) = cancelled

    override fun cancel() {
        cancelled = true
    }
}