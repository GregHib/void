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
import world.gregs.voidps.engine.map.collision.*
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File

val engineModule = module {
    // Entities
    single { NPCs(get(), get(), get()) }
    single { Players() }
    single { GameObjects(get(), get(), get(), get(), getProperty<String>("loadUnusedObjects") == "true").apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItems(get(), get()).apply { get<ZoneBatchUpdates>().register(this) } }
    single { FloorItemTracking(get(), get(), get()) }
    single { Hunting(get(), get(), get(), get(), get(), get()) }
    single {
        SaveQueue(get(), SafeStorage(File(getProperty<String>("storageFailDirectory"))))
    }
    single {
        val homeTile = Tile(
            x = getIntProperty("homeX", 0),
            y = getIntProperty("homeY", 0),
            level = getIntProperty("homeLevel", 0)
        )
        AccountManager(get(), get(), get(), get(), get(), get(), homeTile, get(), get(), get(), get())
    }
    // IO
    single { Yaml(YamlReaderConfiguration(2, 8, VERY_FAST_LOAD_FACTOR)) }
    single { if (getProperty("storage", "") == "database") {
        DatabaseStorage.connect(
            getProperty("database_username"),
            getProperty("database_password"),
            getProperty("database_driver"),
            getProperty("database_jdbc_url"),
            getProperty("database_pool", "2").toInt(),
        )
        val definitions: ItemDefinitions = get()
        DatabaseStorage { definitions.get(it) }
    } else {
        val saves = File(getProperty<String>("savePath"))
        if (!saves.exists()) {
            saves.mkdir()
        }
        FileStorage(get(), saves, get(), getProperty("experienceRate", "1.0").toDouble())
    } }
    single { PlayerAccountLoader(get(), get(), get(), get(), get(), Contexts.Game) }
    // Map
    single { ZoneBatchUpdates() }
    single { DynamicZones(get(), get(), get()) }
    single(createdAtStart = true) { AreaDefinitions().load() }
    // Network
    single {
        ConnectionQueue(getIntProperty("connectionPerTickCap", 1))
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
    single(createdAtStart = true) { RenderEmoteDefinitions().load() }
    single(createdAtStart = true) { MidiDefinitions().load() }
    single(createdAtStart = true) { VariableDefinitions().load() }
    single(createdAtStart = true) { JingleDefinitions().load() }
    single(createdAtStart = true) { SpellDefinitions().load() }
    single(createdAtStart = true) { PrayerDefinitions().load() }
    single(createdAtStart = true) { GearDefinitions().load() }
    single(createdAtStart = true) { ItemOnItemDefinitions().load() }
    single(createdAtStart = true) { AccountDefinitions().load() }
    single(createdAtStart = true) { HuntModeDefinitions().load() }
    single(createdAtStart = true) { CategoryDefinitions().load() }
    single(createdAtStart = true) { ClientScriptDefinitions().load() }
}