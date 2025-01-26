import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import content.bot.TaskManager
import content.bot.interact.navigation.graph.NavigationGraph
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.DijkstraFrontier
import content.entity.player.modal.book.Books
import content.entity.obj.Teleports
import content.entity.world.music.MusicTracks
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