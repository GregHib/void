package world.gregs.voidps.engine.timer

class TimerSlot : Timers {

    private var timer: Timer? = null

    override fun add(interval: Int, cancelExecution: Boolean, block: Timer.(Long) -> Unit): Timer {
        val timer = Timer(interval, cancelExecution, block)
        this.timer?.cancel()
        this.timer = timer
        return timer
    }

    override fun run() {
        val timer = timer ?: return
        if (!timer.ready()) {
            return
        }
        timer.resume()
        if (timer.cancelled) {
            this.timer = null
        }
    }

    override fun clear() {
        timer?.cancel()
        timer = null
    }
}