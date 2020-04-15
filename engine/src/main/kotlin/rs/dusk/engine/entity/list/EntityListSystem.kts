import org.koin.core.context.loadKoinModules
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.list.item.FloorItems
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.obj.Objects
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.list.proj.Projectiles
import rs.dusk.engine.entity.model.*
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.utility.inject

loadKoinModules(entityListModule)

val players: Players by inject()
val npcs: NPCs by inject()
val objects: Objects by inject()
val items: FloorItems by inject()
val projectiles: Projectiles by inject()

Registered priority 9 then {
    when (entity) {
        is Player -> players[entity.tile] = entity
        is NPC -> npcs[entity.tile] = entity
        is IObject -> objects[entity.tile] = entity
        is FloorItem -> items[entity.tile] = entity
        is Projectile -> projectiles[entity.tile] = entity
    }
}