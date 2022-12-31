package world.gregs.voidps.engine.entity.obj

object ObjectType {
    /**
     * A wall that is presented lengthwise with respect to the tile.
     */
    const val LENGTHWISE_WALL = 0

    /**
     * A triangular object positioned in the corner of the tile.
     */
    const val TRIANGULAR_CORNER = 1

    /**
     * A corner for a wall, where the model is placed on two perpendicular edges of a single tile.
     */
    const val WALL_CORNER = 2

    /**
     * A rectangular object positioned in the corner of the tile.
     */
    const val RECTANGULAR_CORNER = 3

    /**
     * An object placed on a wall that can be interacted with by a player.
     */
    const val INTERACTIVE_WALL_DECORATION = 4

    /**
     * A wall that you can interact with.
     */
    const val INTERACTIVE_WALL = 5

    /**
     * A wall joint that is presented diagonally with respect to the tile.
     */
    const val DIAGONAL_WALL = 9

    /**
     * An object that can be interacted with by a player.
     */
    const val INTERACTIVE = 10

    /**
     * An [INTERACTIVE] object, rotated `pi / 2` radians.
     */
    const val DIAGONAL_INTERACTIVE = 11

    /**
     * A decoration positioned on the floor.
     */
    const val FLOOR_DECORATION = 22

    /**
     * Get the [ObjectGroup] for an [ObjectType]
     */
    fun group(type: Int) = when (type) {
        INTERACTIVE, DIAGONAL_INTERACTIVE -> ObjectGroup.INTERACTIVE_OBJECT
        FLOOR_DECORATION -> ObjectGroup.GROUND_DECORATION
        else -> ObjectGroup.WALL
    }
}