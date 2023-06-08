package world.gregs.voidps.engine.map.collision

enum class CollisionFlags {
    WALL_NORTH_WEST,
    WALL_NORTH,
    WALL_NORTH_EAST,
    WALL_EAST,
    WALL_SOUTH_EAST,
    WALL_SOUTH,
    WALL_SOUTH_WEST,
    WALL_WEST,
    OBJECT,
    PROJECTILE_NORTH_WEST,
    PROJECTILE_NORTH,
    PROJECTILE_NORTH_EAST,
    PROJECTILE_EAST,
    PROJECTILE_SOUTH_EAST,
    PROJECTILE_SOUTH,
    PROJECTILE_SOUTH_WEST,
    PROJECTILE_WEST,
    PROJECTILE,
    FLOOR_DECORATION,
    NPCS,
    PLAYERS,
    FLOOR,
    ROUTE_NORTH_WEST,
    ROUTE_NORTH,
    ROUTE_NORTH_EAST,
    ROUTE_EAST,
    ROUTE_SOUTH_EAST,
    ROUTE_SOUTH,
    ROUTE_SOUTH_WEST,
    ROUTE_WEST;

    val bit: Int = 1 shl ordinal

    companion object {
        val wallFlags = intArrayOf(
            WALL_NORTH_WEST.bit,
            WALL_NORTH.bit,
            WALL_NORTH_EAST.bit,
            WALL_EAST.bit,
            WALL_SOUTH_EAST.bit,
            WALL_SOUTH.bit,
            WALL_SOUTH_WEST.bit,
            WALL_WEST.bit
        )

        val projectileFlags = intArrayOf(
            PROJECTILE_NORTH_WEST.bit,
            PROJECTILE_NORTH.bit,
            PROJECTILE_NORTH_EAST.bit,
            PROJECTILE_EAST.bit,
            PROJECTILE_SOUTH_EAST.bit,
            PROJECTILE_SOUTH.bit,
            PROJECTILE_SOUTH_WEST.bit,
            PROJECTILE_WEST.bit
        )

        val routeFlags = intArrayOf(
            ROUTE_NORTH_WEST.bit,
            ROUTE_NORTH.bit,
            ROUTE_NORTH_EAST.bit,
            ROUTE_EAST.bit,
            ROUTE_SOUTH_EAST.bit,
            ROUTE_SOUTH.bit,
            ROUTE_SOUTH_WEST.bit,
            ROUTE_WEST.bit
        )

        val array: IntArray = IntArray(32)
        init {
            for (direction in 0 until 4) {
                for (blockRoute in 0 until 2) {
                    for (blockSky in 0 until 2) {
                        var index = direction or 8
                        var mask = CollisionFlags.wallFlags[direction]
                        if (blockRoute == 0) {
                            mask = mask or CollisionFlags.routeFlags[direction]
                            index = index and 8.inv()
                        }
                        if (blockSky == 1) {
                            mask = mask or CollisionFlags.projectileFlags[direction]
                            index = index or 4
                        }
                        array[index] = mask
                    }
                }
            }
        }
    }
}