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
     * The interactable object group, for objects that can be clicked and interacted with.
     */
    const val INTERACTIVE_OBJECT = 2

    /**
     * The ground decoration object group, which may block a tile.
     */
    const val GROUND_DECORATION = 3
}