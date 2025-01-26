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
import world.gregs.voidps.world.interact.world.spawn.ItemSpawns

/**
 * Defines a Koin module for initializing game-related components and services.
 * The `gameModule` includes the setup and configuration of various game components
 * that are essential for game logic execution such as item spawning, task management,
 * navigation graph construction, and data loading.
 *
 * Components included in this module:
 * - `ItemSpawns`: Manages the spawning and clearing of items in zones.
 * - `TaskManager`: Handles task queuing and task assignment for game entities.
 * - `Dijkstra`: A graph traversal algorithm implementation with a pool of frontiers,
 *   initialized based on the navigation graph's size.
 * - `NavigationGraph`: Sets up the adjacency list for nodes, edges, and areas in the game world.
 * - `Books`: Loads book definitions used in the game.
 * - `MusicTracks`: Manages and loads music track data.
 * - `Teleports`: Loads teleportation data for game shortcuts.
 *
 * This module ensures that specific components like the navigation graph, books, music tracks,
 * and teleports are preloaded and available at the start of the application.
 */
val gameModule = module {
    single { ItemSpawns() }
    single { TaskManager() }
    single {
        val size = get<NavigationGraph>().size
        Dijkstra(
            get(),
            object : DefaultPool<DijkstraFrontier>(10) {
                /**
                 * Produces a new instance of `DijkstraFrontier` with the provided size.
                 *
                 * This method is responsible for creating and returning a new instance of the `DijkstraFrontier` class,
                 * which manages the nodes visited or to be visited by the Dijkstra algorithm.
                 *
                 * @return A new `DijkstraFrontier` instance initialized with the specified size.
                 */
                override fun produceInstance() = DijkstraFrontier(size)
            }
        )
    }
    single(createdAtStart = true) { NavigationGraph(get(), get()).load() }
    single(createdAtStart = true) { Books().load() }
    single(createdAtStart = true) { MusicTracks().load() }
    single(createdAtStart = true) { Teleports().load() }
}