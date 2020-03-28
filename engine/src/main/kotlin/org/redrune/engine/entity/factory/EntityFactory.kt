package org.redrune.engine.entity.factory

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
val entityFactoryModule = module {
    single { FloorItemFactory() }
    single { NPCFactory() }
    single { ObjectFactory() }
    single { PlayerFactory() }
    single { ProjectileFactory() }
}