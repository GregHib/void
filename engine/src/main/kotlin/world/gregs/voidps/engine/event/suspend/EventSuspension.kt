package world.gregs.voidps.engine.event.suspend

interface EventSuspension {
    fun ready(): Boolean
    fun finished(): Boolean
    fun resume()
}