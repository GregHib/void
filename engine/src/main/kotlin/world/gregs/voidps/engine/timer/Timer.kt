package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

class Timer : Timers() {

    private var timer: Job? = null

    override fun add(ticks: Int, loop: Int, cancelExecution: Boolean, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        timer = job
        return job
    }

    override fun tick() {
        val job = timer ?: return
        tick(job)
    }

    override fun add(job: Job) {
        timer = job
    }

    override fun poll() {
        timer = null
    }

    override fun clear() {
        timer?.cancel()
        timer = null
    }
}