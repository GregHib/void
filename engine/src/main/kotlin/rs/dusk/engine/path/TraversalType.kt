package rs.dusk.engine.path

import rs.dusk.engine.map.collision.CollisionFlag

enum class TraversalType(val shift: Int, val block: Int) {
    Land(0, CollisionFlag.BLOCKED),
    Water(0, CollisionFlag.LAND.inv()),
    Sky(9, CollisionFlag.SKY),
    Ignored(22, CollisionFlag.IGNORED);
}