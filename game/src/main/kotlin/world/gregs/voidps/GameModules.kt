package world.gregs.voidps

import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.bot.path.Dijkstra
import world.gregs.voidps.bot.path.DijkstraFrontier

val gameModule = module {
    single { TaskManager() }
    single {
        val size = get<NavigationGraph>().size
        Dijkstra(
            get(),
            object : DefaultPool<DijkstraFrontier>(10) {
                override fun produceInstance() = DijkstraFrontier(size)
            }
        )
    }
}

val postCacheGameModule = module {
    single(createdAtStart = true) { NavigationGraph(get(), get()).load() }
}