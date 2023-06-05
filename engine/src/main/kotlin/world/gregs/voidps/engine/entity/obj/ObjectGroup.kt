package world.gregs.voidps.engine.entity.obj

object ObjectGroup {
    /**
     * The wall object group, which may block a tile.
     */
    const val WALL = 0

    /**
     * The wall decoration object group, which never blocks a tile.
     */
    const val WALL_DECORATION = 1

    /**
     * The interactive object group, for objects that can be clicked and interacted with.
     */
    const val INTERACTIVE = 2

    /**
     * The ground decoration object group, which may block a tile.
     */
    const val GROUND_DECORATION = 3

    private val groups = intArrayOf(
        WALL,
        WALL,
        WALL,
        WALL,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        INTERACTIVE,
        GROUND_DECORATION
    )

    /**
     * Get the [ObjectGroup] for an [ObjectType]
     */
    fun group(type: Int) = groups[type]
}