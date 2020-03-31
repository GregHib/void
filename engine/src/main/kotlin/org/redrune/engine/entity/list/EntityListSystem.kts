import org.koin.core.context.loadKoinModules
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.list.entityListModule
import org.redrune.engine.entity.list.item.FloorItems
import org.redrune.engine.entity.list.npc.NPCs
import org.redrune.engine.entity.list.obj.Objects
import org.redrune.engine.entity.list.player.Players
import org.redrune.engine.entity.list.proj.Projectiles
import org.redrune.engine.entity.model.*
import org.redrune.engine.entity.tile.Tiles
import org.redrune.engine.event.priority
import org.redrune.engine.event.then
import org.redrune.utility.inject

loadKoinModules(entityListModule)

val tiles: Tiles by inject()
val players: Players by inject()
val npcs: NPCs by inject()
val objects: Objects by inject()
val items: FloorItems by inject()
val projectiles: Projectiles by inject()

Registered priority 9 then {
    val tile = tiles[entity]
    when (entity) {
        is Player -> players[tile] = entity
        is NPC -> npcs[tile] = entity
        is IObject -> objects[tile] = entity
        is FloorItem -> items[tile] = entity
        is Projectile -> projectiles[tile] = entity
    }
}