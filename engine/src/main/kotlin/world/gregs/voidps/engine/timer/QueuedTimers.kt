package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop
import java.util.*

class QueuedTimers : Timers() {

    private val queue = PriorityQueue<Job>()

    override fun add(ticks: Int, loop: Int, cancelExecution: Boolean, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        queue.offer(job)
        return job
    }

    override fun add(job: Job) {
        queue.add(job)
    }

    override fun poll() {
        queue.poll()
    }

    override fun run() {
        while (queue.isNotEmpty()) {
            val job = queue.peek()
            if (!tick(job)) {
                break
            }
        }
    }

    override fun clear() {
        for (job in queue) {
            job.cancel()
        }
        queue.clear()
    }
}