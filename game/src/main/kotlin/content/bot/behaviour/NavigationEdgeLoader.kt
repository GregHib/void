package content.bot.behaviour

import content.bot.behaviour.navigation.NavigationGraph
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

/**
 * The edges of the navigation graph as parsed from config, snapshotted separately as turning them into actions and conditions can't be.
 */
object NavigationEdgeLoader : Loader<List<NavigationGraph.Companion.Edge>> {
    override val name = "nav_edges"
    override val label = "nav graph edge"
    override val files = listOf("bots.nav.definitions")
    override val codec = SnapshotFormat.codec(NavigationEdgesSerializer)

    override fun load(context: LoadContext): List<NavigationGraph.Companion.Edge> = NavigationGraph.parseEdges(context.paths(this))

    override fun total(state: List<NavigationGraph.Companion.Edge>) = state.size

    override fun install(state: List<NavigationGraph.Companion.Edge>) {}
}
