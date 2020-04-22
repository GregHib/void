package rs.dusk.engine

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
data class EngineTasks(val delegate: MutableList<Runnable> = mutableListOf()) : MutableList<Runnable> by delegate

val engineModule = module {
    single { EngineTasks() }
}