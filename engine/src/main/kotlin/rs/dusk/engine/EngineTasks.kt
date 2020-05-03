package rs.dusk.engine

import org.koin.dsl.module
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
data class EngineTasks(val data: TreeSet<EngineTask> = TreeSet()) : MutableSet<EngineTask> by data

val engineModule = module {
    single { EngineTasks() }
}