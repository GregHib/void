package rs.dusk.engine.model.entity.factory

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
val entityFactoryModule = module {
    single { NPCFactory() }
    single { PlayerFactory() }
}