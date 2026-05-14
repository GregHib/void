package world.gregs.voidps.engine.queue

enum class ActionPriority {
    /**
     * Removed by interruptions and [Strong] actions
     */
    Weak,

    /**
     * Skipped if an interface is open
     */
    Normal,

    /**
     * Normal but executed immediately on logout
     */
    Long,

    /**
     * Closes interfaces and cancels [Weak] actions before execution
     */
    Strong,
}
