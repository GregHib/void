package world.gregs.voidps.engine

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.definition.*
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.map.file.MapExtract
import world.gregs.voidps.engine.map.file.MapObjectLoader
import world.gregs.voidps.engine.map.nav.NavigationGraph

/**
 * Modules which depend on cache definitions
 */
val postCacheModule = module {
    single { GameObjectFactory(get(), get(), get()) }
    single { MapExtract(get(), MapObjectLoader(get(), get(), get(), get())) }
    single(createdAtStart = true) { CustomObjects(get(), get(), get(), get(), get()) }
    single(createdAtStart = true) { NavigationGraph(get(), get()).load() }
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