import org.koin.core.context.loadKoinModules
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.list.entityListModule
import org.redrune.engine.entity.list.item.FloorItems
import org.redrune.engine.entity.list.npc.NPCs
import org.redrune.engine.entity.list.obj.Objects
import org.redrune.engine.entity.list.player.Players
import org.redrune.engine.entity.list.proj.Projectiles
import org.redrune.engine.entity.model.*
import org.redrune.engine.event.priority
import org.redrune.engine.event.then
import org.redrune.utility.inject

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