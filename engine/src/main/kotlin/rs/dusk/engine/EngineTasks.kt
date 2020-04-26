package rs.dusk.engine

import org.koin.dsl.module

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
data class EngineTasks(val data: MutableList<EngineTask> = mutableListOf()) : MutableList<EngineTask> by data

val engineModule = module {
    single { EngineTasks() }
}