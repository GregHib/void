package rs.dusk.tools.map.process

import rs.dusk.tools.map.view.graph.GraphIO
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.io.File

/**
 * Dumps world map links from a decoded cs2 script into nav graph format.
 */
object WorldMapLinkDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val script = File("C:\\Users\\Greg\\Documents\\decompiled cs2\\295.cs2")
        val regex = "script_297\\(location\\((.*?),\\s(.*?),\\s(.*?)\\), location\\((.*?),\\s(.*?),\\s(.*?)\\)".toRegex()
        val graph = NavigationGraph()
        regex.findAll(script.readText()).forEach {
            val vals = it.groupValues
            val x = vals[1].toInt()
            val y = vals[2].toInt()
            val z = vals[3].toInt()
            val x2 = vals[4].toInt()
            val y2 = vals[5].toInt()
            val z2 = vals[6].toInt()
            graph.createJointLink(x, y, z, x2, y2, z2)
            graph.createJointLink(x2, y2, z2, x, y, z)
        }
        GraphIO(graph, "./worldmaplinks.json").save()
    }
}