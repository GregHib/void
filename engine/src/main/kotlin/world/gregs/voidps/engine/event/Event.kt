package world.gregs.voidps.engine.event

interface Event {

    fun size(): Int

    fun parameter(dispatcher: EventDispatcher, index: Int): Any?

    fun debug(dispatcher: EventDispatcher): String = Array(size()) { parameter(dispatcher, it) }.contentToString()
}