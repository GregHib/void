package world.gregs.voidps.engine.entity.item

/**
 * What should happen to an item when a player dies with it?
 */
enum class ItemKept {
    /**
     * Dropped onto the floor
     */
    Never,

    /**
     * If dropped, it vanishes and has to be reclaimed elsewhere
     */
    Reclaim,

    /**
     * Kept safe in gravestone unless in the wilderness where it is dropped
     */
    Wilderness,

    /**
     * Always saved regardless
     */
    Always,

    /**
     * Disappears regardless of if kept on death
     */
    Vanish,

    ;

    companion object {
        private val map = mapOf(
            "Never" to Never,
            "Reclaim" to Reclaim,
            "Wilderness" to Wilderness,
            "Always" to Always,
            "Vanish" to Vanish,
        )

        fun by(name: String): ItemKept = map[name] ?: Never
    }
}
