import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.character.Character
import rs.dusk.engine.model.entity.character.Moved
import rs.dusk.engine.model.entity.character.npc.NPCRegistered
import rs.dusk.engine.model.entity.character.player.PlayerRegistered
import rs.dusk.engine.model.map.collision.CollisionFlag.ENTITY
import rs.dusk.engine.model.map.collision.Collisions
import rs.dusk.engine.model.map.collision.add
import rs.dusk.engine.model.map.collision.remove
import rs.dusk.utility.inject

val collisions: Collisions by inject()

PlayerRegistered priority 9 then {
    collisions.add(player.tile.x, player.tile.y, player.tile.plane, ENTITY)
}

NPCRegistered priority 9 then {
    for (x in 0 until npc.size.width) {
        for (y in 0 until npc.size.height) {
            collisions.add(npc.tile.x + x, npc.tile.y + y, npc.tile.plane, ENTITY)
        }
    }
}

Unregistered priority 9 where { entity is Character } then {
    entity as Character
    collisions.remove(entity.tile.x, entity.tile.y, entity.tile.plane, ENTITY)
}

Moved priority 9 where { entity is Character } then {
    val entity = entity as Character
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