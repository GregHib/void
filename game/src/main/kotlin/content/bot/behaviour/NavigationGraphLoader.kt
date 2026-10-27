package content.bot.behaviour

import content.bot.behaviour.navigation.NavigationGraph
import world.gregs.voidps.engine.data.definition.AreaLoader
import world.gregs.voidps.engine.data.definition.InterfaceLoader
import world.gregs.voidps.engine.data.definition.ItemLoader
import world.gregs.voidps.engine.data.definition.TableLoader
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object NavigationGraphLoader : Loader<NavigationGraph> {
    lateinit var graph: NavigationGraph
        private set
    override val name = "nav_graph"
    override val files = listOf("bots.templates", "bots.shortcuts")
    override val dependsOn: List<Loader<*>> get() = listOf(NavigationEdgeLoader, AreaLoader, InterfaceLoader, ItemLoader, TableLoader)

    override fun load(context: LoadContext): NavigationGraph {
        val shortcuts = loadNavigationShortcuts(context.list("bots.templates"), context.list("bots.shortcuts"))
        return NavigationGraph.loadGraph(context[NavigationEdgeLoader], shortcuts)
    }

    override fun install(state: NavigationGraph) {
        graph = state
    }
}
