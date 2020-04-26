import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.list.item.FloorItems
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.obj.Objects
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.list.proj.Projectiles
import rs.dusk.engine.entity.model.*
import rs.dusk.engine.entity.model.visual.visuals.player.face
import rs.dusk.engine.entity.model.visual.visuals.player.movementSpeed
import rs.dusk.engine.entity.model.visual.visuals.player.movementType
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.utility.inject

val players: Players by inject()
val npcs: NPCs by inject()
val objects: Objects by inject()
val items: FloorItems by inject()
val projectiles: Projectiles by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> {
            players[entity.tile] = entity
            players[entity.tile.chunk] = entity
            players.addAtIndex(entity.index, entity)
            entity.viewport.players.current.add(entity)
            entity.movementSpeed = false
            entity.movementType = 0
            entity.face()
        }
        is NPC -> npcs[entity.tile] = entity
        is IObject -> objects[entity.tile] = entity
        is FloorItem -> items[entity.tile] = entity
        is Projectile -> projectiles[entity.tile] = entity
    }
}