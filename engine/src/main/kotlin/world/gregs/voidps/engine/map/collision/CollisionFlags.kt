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
        val wallFlags = arrayOf(
            WALL_NORTH_WEST,
            WALL_NORTH,
            WALL_NORTH_EAST,
            WALL_EAST,
            WALL_SOUTH_EAST,
            WALL_SOUTH,
            WALL_SOUTH_WEST,
            WALL_WEST
        )

        val projectileFlags = arrayOf(
            PROJECTILE_NORTH_WEST,
            PROJECTILE_NORTH,
            PROJECTILE_NORTH_EAST,
            PROJECTILE_EAST,
            PROJECTILE_SOUTH_EAST,
            PROJECTILE_SOUTH,
            PROJECTILE_SOUTH_WEST,
            PROJECTILE_WEST
        )

        val routeFlags = arrayOf(
            ROUTE_NORTH_WEST,
            ROUTE_NORTH,
            ROUTE_NORTH_EAST,
            ROUTE_EAST,
            ROUTE_SOUTH_EAST,
            ROUTE_SOUTH,
            ROUTE_SOUTH_WEST,
            ROUTE_WEST
        )
    }
}