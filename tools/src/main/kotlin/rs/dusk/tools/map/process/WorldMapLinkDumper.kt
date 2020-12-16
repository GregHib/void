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
//            graph.createLink(vals[1].toInt(), vals[2].toInt(), vals[3].toInt(), vals[4].toInt(), vals[5].toInt(), vals[6].toInt())
        }
        GraphIO(graph).save()
    }
}