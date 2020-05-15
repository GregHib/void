import rs.dusk.engine.entity.list.item.FloorItems
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.obj.Objects
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.list.proj.Projectiles
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.visual.player.face
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.engine.model.entity.index.update.visual.player.temporaryMoveType
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.obj.IObject
import rs.dusk.engine.model.entity.proj.Projectile
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
            entity.viewport.players.add(entity)
            entity.temporaryMoveType = PlayerMoveType.Walk
            entity.movementType = PlayerMoveType.None
            entity.face()
        }
        is NPC -> {
            npcs[entity.tile] = entity
            npcs[entity.tile.chunk] = entity
            npcs.addAtIndex(entity.index, entity)
        }
        is IObject -> objects[entity.tile] = entity
        is FloorItem -> items[entity.tile] = entity
        is Projectile -> projectiles[entity.tile] = entity
    }
}