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
        val blocked = intArrayOf(
            WALL_NORTH_WEST.bit,
            WALL_NORTH.bit,
            WALL_NORTH_EAST.bit,
            WALL_EAST.bit,
            WALL_SOUTH_EAST.bit,
            WALL_SOUTH.bit,
            WALL_SOUTH_WEST.bit,
            WALL_WEST.bit,
            WALL_NORTH_WEST.bit or PROJECTILE_NORTH_WEST.bit,
            WALL_NORTH.bit or PROJECTILE_NORTH.bit,
            WALL_NORTH_EAST.bit or PROJECTILE_NORTH_EAST.bit,
            WALL_EAST.bit or PROJECTILE_EAST.bit,
            WALL_SOUTH_EAST.bit or PROJECTILE_SOUTH_EAST.bit,
            WALL_SOUTH.bit or PROJECTILE_SOUTH.bit,
            WALL_SOUTH_WEST.bit or PROJECTILE_SOUTH_WEST.bit,
            WALL_WEST.bit or PROJECTILE_WEST.bit,
            WALL_NORTH_WEST.bit or ROUTE_NORTH_WEST.bit,
            WALL_NORTH.bit or ROUTE_NORTH.bit,
            WALL_NORTH_EAST.bit or ROUTE_NORTH_EAST.bit,
            WALL_EAST.bit or ROUTE_EAST.bit,
            WALL_SOUTH_EAST.bit or ROUTE_SOUTH_EAST.bit,
            WALL_SOUTH.bit or ROUTE_SOUTH.bit,
            WALL_SOUTH_WEST.bit or ROUTE_SOUTH_WEST.bit,
            WALL_WEST.bit or ROUTE_WEST.bit,
            WALL_NORTH_WEST.bit or ROUTE_NORTH_WEST.bit or PROJECTILE_NORTH_WEST.bit,
            WALL_NORTH.bit or ROUTE_NORTH.bit or PROJECTILE_NORTH.bit,
            WALL_NORTH_EAST.bit or ROUTE_NORTH_EAST.bit or PROJECTILE_NORTH_EAST.bit,
            WALL_EAST.bit or ROUTE_EAST.bit or PROJECTILE_EAST.bit,
            WALL_SOUTH_EAST.bit or ROUTE_SOUTH_EAST.bit or PROJECTILE_SOUTH_EAST.bit,
            WALL_SOUTH.bit or ROUTE_SOUTH.bit or PROJECTILE_SOUTH.bit,
            WALL_SOUTH_WEST.bit or ROUTE_SOUTH_WEST.bit or PROJECTILE_SOUTH_WEST.bit,
            WALL_WEST.bit or ROUTE_WEST.bit or PROJECTILE_WEST.bit
        )
        val inverse: IntArray = IntArray(32) { blocked[it].inv() }
    }
}