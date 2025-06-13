package world.gregs.voidps.engine.entity.obj

object ObjectLayer {

    /**
     * The wall object, which may block a tile.
     */
    const val WALL = 0

    /**
     * The wall decoration object, which never blocks a tile.
     */
    const val WALL_DECORATION = 1

    /**
     * The interactive object, for objects that can be clicked and interacted with.
     */
    const val GROUND = 2

    /**
     * The ground decoration object, which may block a tile.
     */
    const val GROUND_DECORATION = 3

    private val layers = intArrayOf(
        WALL,
        WALL,
        WALL,
        WALL,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        WALL_DECORATION,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND,
        GROUND_DECORATION,
    )

    /**
     * Get the [ObjectLayer] for an [ObjectShape]
     */
    fun layer(shape: Int) = layers[shape]
}
