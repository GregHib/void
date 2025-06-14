package world.gregs.voidps.engine.entity.obj

object ObjectShape {
    /**
     * A wall that is presented lengthwise with respect to the tile.
     */
    const val WALL_STRAIGHT = 0

    /**
     * A triangular object positioned in the corner of the tile.
     */
    const val WALL_DIAGONAL_CORNER = 1

    /**
     * A corner for a wall, where the model is placed on two perpendicular edges of a single tile.
     */
    const val WALL_CORNER = 2

    /**
     * A rectangular object positioned in the corner of the tile.
     */
    const val WALL_SQUARE_CORNER = 3

    /**
     * An object placed on a wall that can be interacted with by a player.
     */
    const val WALL_DECOR_STRAIGHT_NO_OFFSET = 4

    /**
     * A wall that you can interact with.
     */
    const val WALL_DECOR_STRAIGHT_OFFSET = 5

    const val WALL_DECOR_DIAGONAL_OFFSET = 6

    const val WALL_DECOR_DIAGONAL_NO_OFFSET = 7

    const val WALL_DECOR_DIAGONAL_BOTH = 8

    /**
     * A wall joint that is presented diagonally with respect to the tile.
     */
    const val WALL_DIAGONAL = 9

    /**
     * An object that can be interacted with by a player.
     */
    const val CENTRE_PIECE_STRAIGHT = 10

    /**
     * A [CENTRE_PIECE_STRAIGHT] object, rotated `pi / 2` radians.
     */
    const val CENTRE_PIECE_DIAGONAL = 11

    const val ROOF_STRAIGHT = 12

    const val ROOF_DIAGONAL_WITH_ROOF_EDGE = 13

    const val ROOF_DIAGONAL = 14

    const val ROOF_CORNER_CONCAVE = 15

    const val ROOF_CORNER_CONVEX = 16

    const val ROOF_FLAT = 17

    const val ROOF_EDGE_STRAIGHT = 18

    const val ROOF_EDGE_DIAGONAL_CORNER = 19

    const val ROOF_EDGE_CORNER = 20

    const val ROOF_EDGE_SQUARE_CORNER = 21

    /**
     * A decoration positioned on the floor.
     */
    const val GROUND_DECOR = 22

    const val WALL_CORNER_ALT = 23

    const val WALL_DECOR_DIAGONAL_BOTH_ALT = 24

    fun isWall(shape: Int): Boolean = shape == WALL_STRAIGHT || shape in WALL_DECOR_STRAIGHT_NO_OFFSET..WALL_DIAGONAL

    fun isCorner(shape: Int) = shape in WALL_DIAGONAL_CORNER..WALL_SQUARE_CORNER
}
