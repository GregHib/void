import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Deregistered
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.*
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.entity.proj.Projectile
import rs.dusk.engine.model.entity.proj.Projectiles
import rs.dusk.utility.inject

val players: Players by inject()
val npcs: NPCs by inject()
val objects: Objects by inject()
val items: FloorItems by inject()
val projectiles: Projectiles by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> {
            players.add(entity)
            entity.viewport.players.add(entity)
            entity.temporaryMoveType = PlayerMoveType.None
            entity.movementType = PlayerMoveType.None
            entity.flagMovementType()
            entity.flagTemporaryMoveType()
            entity.face()
        }
        is NPC -> npcs.add(entity)
        is Location -> {
            objects[entity.tile] = entity
        }
        is FloorItem -> items.add(entity)
        is Projectile -> projectiles[entity.tile] = entity
    }
}

Deregistered priority 9 then {
    when (entity) {
        is Player -> players.remove(entity)
        is NPC -> npcs.remove(entity)
        is Location -> objects.remove(entity.tile, entity)
        is FloorItem -> items.remove(entity)
        is Projectile -> projectiles.remove(entity.tile, entity)
    }
}

Moved priority 9 where { entity is Character } then {
    when(entity) {
        is Player -> players.update(from, to, entity)
        is NPC -> npcs.update(from, to, entity)
    }
}