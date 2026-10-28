package world.gregs.voidps.engine

import org.koin.dsl.module
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.entity.character.npc.NPCSpawnLoader
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.equip.AppearanceOverrides
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.DropTablesLoader
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.network.client.ConnectionQueue
import java.io.File

fun engineLoad() {
    ZoneBatchUpdates.register(GameObjects)
    ZoneBatchUpdates.register(FloorItems)
}

fun engineModule() = module {
    // Entities
    single { Hunting(get(), get()) }
    single {
        val storage: Storage = get()
        val bots = if (Settings["storage.bots.save", false] && Settings["storage.bots.separate", false] && Settings["storage.type", "files"] == "files") {
            FileStorage(File(Settings["storage.bots.path"]).apply { mkdirs() })
        } else {
            storage
        }
        SaveQueue(storage, SafeStorage(File(Settings["storage.players.errors"])), botStorage = bots)
    }
    single { AccountManager(get(), get(), get(), AppearanceOverrides().apply { load() }) }
    single { AccountDefinitionsReloader(get(), get(), get()) }
    // IO
    single { PlayerAccountLoader(get(), get(), get(), get(), get(), Contexts.Game) }
    single(createdAtStart = true) { AccountDefinitions().load() }
    // Map
    single { DynamicZones(get()) }
    // Network
    single {
        ConnectionQueue(Settings["network.maxLoginsPerTick", 1])
    }
    single(createdAtStart = true) { GameObjectCollisionAdd() }
    single(createdAtStart = true) { GameObjectCollisionRemove() }
    // Collision
    single { StepValidator(Collisions.map) }
    // Pathfinding
    single { PathFinder(flags = Collisions.map, useRouteBlockerFlags = true) }
    single { LineValidator(flags = Collisions.map) }
}

/**
 * Loaders of every definition and config in the engine.
 */
fun engineLoaders(): List<Loader<*>> = listOf(
    ItemLoader,
    ObjectLoader,
    NPCLoader,
    DropTablesLoader,
    NPCSpawnLoader,
    AnimationLoader,
    GraphicLoader,
    InterfaceLoader,
    InventoryLoader,
    StructLoader,
    VariableLoader,
    TableLoader,
    EnumLoader,
    SoundLoader,
    FontLoader,
    QuickChatPhraseLoader,
    CombatLoader,
    AreaLoader,
    ItemOnItemLoader,
    RenderEmoteLoader,
    MidiLoader,
    JingleLoader,
    PatrolLoader,
    ClientScriptLoader,
    QuestLoader,
    PrayerLoader,
    HuntModeLoader,
    MapLoader,
)

/**
 * Makes the results of [engineLoaders] available through koin.
 */
fun definitionsModule() = module {
    single { ItemDefinitions }
    single { ObjectDefinitions }
    single { NPCDefinitions }
    single { AnimationDefinitions }
    single { GraphicDefinitions }
    single { InterfaceDefinitions }
    single { InventoryDefinitions }
    single { StructDefinitions }
    single { EnumDefinitions }
    single { Tables }
    single { VariableDefinitions }
    single<DropTables> { DropTablesLoader.instance }
    single { SoundLoader.instance }
    single { FontLoader.instance }
    single { QuickChatPhraseLoader.instance }
    single { CombatLoader.instance }
    single { ItemOnItemLoader.instance }
    single { RenderEmoteLoader.instance }
    single { MidiLoader.instance }
    single { JingleLoader.instance }
    single { PatrolLoader.instance }
    single { ClientScriptLoader.instance }
    single { QuestLoader.instance }
    single { PrayerLoader.instance }
    single { HuntModeLoader.instance }
}
