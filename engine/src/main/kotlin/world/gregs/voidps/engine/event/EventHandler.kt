package world.gregs.voidps.engine.event

data class EventHandler(
    val condition: Event.(EventDispatcher) -> Boolean = { true },
    val priority: Priority = Priority.MEDIUM,
    val block: suspend Event.(EventDispatcher) -> Unit = {}
) : Comparable<EventHandler> {
    override fun compareTo(other: EventHandler): Int {
        return other.priority.ordinal.compareTo(priority.ordinal)
    }
}