package world.gregs.voidps.engine

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.file.MapExtract
import world.gregs.voidps.engine.map.file.MapObjectLoader
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas

val engineModule = module {
    // Entities
    single { NPCs(get(), get(), get(), get()) }
    single { Players() }
    single { Objects() }
    single { FloorItems(get(), get(), get(), get()) }
    single {
        PlayerFactory(get(), get(), get(), get(), get(), get(named("jsonStorage")), getProperty("savePath"), get(), get(), Tile(
            getIntProperty("homeX", 0), getIntProperty("homeY", 0), getIntProperty("homePlane", 0)
        ))
    }
    // IO
    single { FileStorage() }
    single(named("jsonStorage")) { FileStorage(json = true) }
    // Map
    single { ChunkBatchUpdates(get()) }
    single { DynamicChunks(get(), get(), get()) }
    single { EventHandlerStore() }
    single(createdAtStart = true) { Areas().load() }
    single(createdAtStart = true) {
        Xteas(mutableMapOf()).apply {
            XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
        }
    }
    // Network
    single {
        ConnectionQueue(getIntProperty("connectionPerTickCap", 1))
    }
    single { ConnectionGatekeeper(get()) }
    single(createdAtStart = true) { GameObjectCollision(get()) }
    // Collision
    single { Collisions() }
    single { CollisionStrategyProvider() }
    single { StepValidator(get<Collisions>()) }
    // Pathfinding
    single { PathFinder(flags = get<Collisions>(), useRouteBlockerFlags = true) }
    single { LineValidator(flags = get<Collisions>()) }
    // Misc
    single(createdAtStart = true) { DropTables().load() }
}

/**
 * Modules which depend on cache definitions
 */
val postCacheModule = module {
    single { GameObjectFactory(get(), get()) }
    single { MapExtract(get(), MapObjectLoader(get(), get(), get(), get())) }
    single(createdAtStart = true) { CustomObjects(get(), get(), get(), get()) }
    // Definitions
    single(createdAtStart = true) { SoundDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { VariableDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { ItemOnItemDefinitions().load() }
    single(createdAtStart = true) { AccountDefinitions(get()).load() }
}