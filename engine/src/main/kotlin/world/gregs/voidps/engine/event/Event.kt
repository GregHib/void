package world.gregs.voidps.engine.event

interface Event {

    /**
     * Search for all handlers matching, not just the first.
     */
    val all: Boolean
        get() = false

    val size: Int

    fun parameter(dispatcher: EventDispatcher, index: Int): Any?

    fun debug(dispatcher: EventDispatcher): String = Array(size) { parameter(dispatcher, it) }.contentToString()
}