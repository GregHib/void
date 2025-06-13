package world.gregs.voidps.engine.queue

enum class ActionPriority(
    val closeInterfaces: Boolean = false,
) {
    /**
     * Removed by interruptions and [Strong] actions
     */
    Weak,

    /**
     * Skipped if an interface is open
     */
    Normal,

    /**
     * Closes interfaces and cancels [Weak] actions before execution
     */
    Strong(closeInterfaces = true),

    /**
     * Closes interfaces and can't be paused or canceled by anything other than suspensions
     */
    Soft(closeInterfaces = true),
}
