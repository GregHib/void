package world.gregs.voidps.engine.event

interface Event {
    /**
     * Event gets sent to all applicable handlers not just the first
     */
    val notification: Boolean
        get() = false

    val size: Int

    fun parameter(dispatcher: EventDispatcher, index: Int): Any?

    fun debug(dispatcher: EventDispatcher): String = Array(size) { parameter(dispatcher, it) }.contentToString()
}
