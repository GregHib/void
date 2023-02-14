package world.gregs.voidps.engine.timer

import java.util.*

class TimerQueue : Timers {

    private val queue = PriorityQueue<Timer>()

    override fun add(interval: Int, cancelExecution: Boolean, block: Timer.(Long) -> Unit): Timer {
        val timer = Timer(interval, cancelExecution, block)
        queue.add(timer)
        return timer
    }

    override fun run() {
        val it = queue.iterator()
        var timer: Timer
        while (it.hasNext()) {
            timer = it.next()
            if (!timer.ready()) {
                break
            }
            timer.resume()
            if (timer.cancelled) {
                it.remove()
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