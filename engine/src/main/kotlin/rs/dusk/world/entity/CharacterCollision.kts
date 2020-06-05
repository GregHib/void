import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Deregistered
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.map.collision.CollisionFlag.ENTITY
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.add
import rs.dusk.engine.model.world.map.collision.remove
import rs.dusk.utility.inject

val collisions: Collisions by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> collisions.add(entity.tile.x, entity.tile.y, entity.tile.plane, ENTITY)
        is NPC -> {
            for (x in 0 until entity.size.width) {
                for (y in 0 until entity.size.height) {
                    collisions.add(entity.tile.x + x, entity.tile.y + y, entity.tile.plane, ENTITY)
                }
            }
        }
    }
}

Deregistered priority 9 where { entity is Indexed } then {
    entity as Indexed
    collisions.remove(entity.tile.x, entity.tile.y, entity.tile.plane, ENTITY)
}

Moved priority 9 where { entity is Indexed } then {
    entity as Indexed
    // No simple way of looking up if an npc is over a tile (incl size)
    // This means players can remove npcs collisions.
    for (x in 0 until entity.size.width) {
        for (y in 0 until entity.size.height) {
            collisions.remove(from.x + x, from.y + y, from.plane, ENTITY)
        }
    }
    for (x in 0 until entity.size.width) {
        for (y in 0 until entity.size.height) {
            collisions.add(to.x + x, to.y + y, to.plane, ENTITY)
        }
    }
}