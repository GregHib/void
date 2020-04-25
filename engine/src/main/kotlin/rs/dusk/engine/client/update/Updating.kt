package rs.dusk.engine.client.update

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
val clientUpdateModule = module {
    single(createdAtStart = true) { PreUpdateTask(get()) }
}