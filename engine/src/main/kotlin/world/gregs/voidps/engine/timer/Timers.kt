package world.gregs.voidps.engine.timer

interface Timers : Runnable {
    fun start(name: String, restart: Boolean = false): Boolean
    fun restart(name: String) = start(name, restart = true)
    fun contains(name: String): Boolean

    /**
     * Clears timer [name] and emits [TimerApi.stop]
     */
    fun stop(name: String)

    /**
     * Removes timer [name]
     */
    fun clear(name: String): Boolean

    /**
     * Stops all timers
     */
    fun stopAll()

    /**
     * Removes all timers
     */
    fun clearAll()

    fun toggle(name: String) {
        if (contains(name)) {
            stop(name)
            return
        }
        start(name)
    }

    fun startIfAbsent(name: String) {
        if (contains(name)) {
            return
        }
        start(name)
    }
}
