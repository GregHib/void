package world.gregs.voidps.engine.timer

class TimerSlot : Timers {

    private var timer: Timer? = null

    override fun add(name: String, interval: Int, cancelExecution: Boolean, block: Timer.(Long) -> Unit): Timer {
        val timer = Timer(name, interval, cancelExecution, block)
        this.timer?.cancel()
        this.timer = timer
        return timer
    }

    override fun contains(name: String): Boolean {
        return timer?.name == name
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

    override fun clear(name: String) {
        if (contains(name)) {
            clearAll()
        }
    }

    override fun clearAll() {
        timer?.cancel()
        timer = null
    }
}