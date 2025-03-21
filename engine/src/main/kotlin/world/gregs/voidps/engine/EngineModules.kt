package world.gregs.voidps.engine

import it.unimi.dsi.fastutil.Hash.VERY_FAST_LOAD_FACTOR
import org.koin.dsl.module
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.SafeStorage
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.json.FileStorage
import world.gregs.voidps.engine.data.sql.DatabaseStorage
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItemTracking
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File

val engineModule = module {
    // Entities
    single { NPCs(get(), get(), get(), get()) }
    single { Players() }
    single { GameObjects(get(), get(), get(), get(), Settings["development.loadAllObjects", false]).apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItems(get(), get()).apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItemTracking(get(), get(), get()) }
    single { Hunting(get(), get(), get(), get(), get(), get()) }
    single {
        SaveQueue(get(), SafeStorage(File(Settings["storage.players.errors"])))
    }
    single { AccountManager(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    // IO
    single { Yaml(YamlReaderConfiguration(2, 8, VERY_FAST_LOAD_FACTOR)) }
    single {
        if (Settings["storage.type", "files"] == "database") {
            DatabaseStorage.connect(
                Settings["storage.database.username"],
                Settings["storage.database.password"],
                Settings["storage.database.driver"],
                Settings["storage.database.jdbcUrl"],
                Settings["storage.database.poolSize", 2],
            )
            DatabaseStorage()
        } else {
            val saves = File(Settings["storage.players.path"])
            if (!saves.exists()) {
                saves.mkdir()
            }
            FileStorage(get(), saves)
        }
    }
    single { PlayerAccountLoader(get(), get(), get(), get(), get(), Contexts.Game) }
    // Map
    single { ZoneBatchUpdates() }
    single { DynamicZones(get(), get(), get()) }
    single(createdAtStart = true) { AreaDefinitions().load() }
    single(createdAtStart = true) { CanoeDefinitions().load() }
    // Network
    single {
        ConnectionQueue(Settings["network.maxLoginsPerTick", 1])
    }
    single(createdAtStart = true) { GameObjectCollisionAdd(get()) }
    single(createdAtStart = true) { GameObjectCollisionRemove(get()) }
    // Collision
    single { Collisions() }
    single { CollisionStrategyProvider() }
    single { StepValidator(get<Collisions>()) }
    // Pathfinding
    single { PathFinder(flags = get<Collisions>(), useRouteBlockerFlags = true) }
    single { LineValidator(flags = get<Collisions>()) }
    // Misc
    single(createdAtStart = true) { DropTables().load(itemDefinitions = get()) }
    // Definitions
    single(createdAtStart = true) { SoundDefinitions().load() }
    single(createdAtStart = true) { QuestDefinitions().load() }
    single(createdAtStart = true) { RenderEmoteDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
    single(createdAtStart = true) { PatrolDefinitions().load() }
    single(createdAtStart = true) { PrayerDefinitions().load() }
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { DiangoCodeDefinitions().load() }
    single(createdAtStart = true) { AccountDefinitions().load() }
    single(createdAtStart = true) { HuntModeDefinitions().load() }
    single(createdAtStart = true) { CategoryDefinitions().load() }
    single(createdAtStart = true) { ClientScriptDefinitions().load() }
}