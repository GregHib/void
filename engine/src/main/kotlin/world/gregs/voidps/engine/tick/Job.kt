package world.gregs.voidps.engine.tick

class Job(
    var tick: Long,
    var loop: Int,
    val block: Job.(Long) -> Unit
) : Comparable<Job> {
    var cancelled = false
        private set

    fun cancel() {
        cancelled = true
    }

    override fun compareTo(other: Job): Int {
        return tick.compareTo(other.tick)
    }
}