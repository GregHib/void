package world.gregs.voidps

import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.bot.TaskManager
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.bot.path.Dijkstra
import world.gregs.voidps.bot.path.DijkstraFrontier
import world.gregs.voidps.world.activity.quest.Books
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import content.entity.item.spawn.ItemSpawns

val gameModule = module {
    single { ItemSpawns() }
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
    single(createdAtStart = true) { NavigationGraph(get(), get()).load() }
    single(createdAtStart = true) { Books().load() }
    single(createdAtStart = true) { MusicTracks().load() }
    single(createdAtStart = true) { Teleports().load() }
}