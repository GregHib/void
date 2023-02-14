package world.gregs.voidps.engine.timer

import java.util.*

class TimerQueue : Timers {

    private val queue = PriorityQueue<Timer>()

    override fun add(name: String, interval: Int, cancelExecution: Boolean, block: Timer.(Long) -> Unit): Timer {
        val timer = Timer(name, interval, cancelExecution, block)
        queue.add(timer)
        return timer
    }

    override fun contains(name: String): Boolean {
        return queue.any { it.name == name }
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

    override fun clear(name: String) {
        queue.removeIf {
            if (it.name == name) {
                it.cancel()
                true
            } else {
                false
            }
        }
    }

    override fun clearAll() {
        for (job in queue) {
            job.cancel()
        }
        queue.clear()
    }
}