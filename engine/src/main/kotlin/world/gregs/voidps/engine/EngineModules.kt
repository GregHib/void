package world.gregs.voidps.engine

import it.unimi.dsi.fastutil.Hash.VERY_FAST_LOAD_FACTOR
import org.koin.dsl.module
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItemTracking
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

val engineModule = module {
    // Entities
    single { NPCs(get(), get(), get(), get()) }
    single { Players() }
    single { GameObjects(get(), get(), get(), getProperty<String>("loadUnusedObjects") == "true").apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItems(get(), get(), get()).apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItemTracking(get(), get(), get()) }
    single { Hunting(get(), get(), get(), get(), get(), get()) }
    single {
        PlayerAccounts(get(), get(), get(), get(), get(), get(), getProperty("savePath"), get(), get(), Tile(
            getIntProperty("homeX", 0), getIntProperty("homeY", 0), getIntProperty("homeLevel", 0)
        ))
    }
    // IO
    single { Yaml(YamlReaderConfiguration(2, 8, VERY_FAST_LOAD_FACTOR)) }
    // Map
    single { ZoneBatchUpdates() }
    single { DynamicZones(get(), get(), get()) }
    single { EventHandlerStore() }
    single(createdAtStart = true) { AreaDefinitions().load() }
    single(createdAtStart = true) { Xteas().load() }
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
    // Definitions
    single(createdAtStart = true) { SoundDefinitions().load() }
    single(createdAtStart = true) { RenderEmoteDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { VariableDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { ItemOnItemDefinitions().load() }
    single(createdAtStart = true) { AccountDefinitions().load() }
    single(createdAtStart = true) { HuntModeDefinitions().load() }
    single(createdAtStart = true) { AmmoDefinitions().load() }
    single(createdAtStart = true) { CategoryDefinitions().load() }
    single(createdAtStart = true) { ParameterDefinitions().load() }
}