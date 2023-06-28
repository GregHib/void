package world.gregs.voidps.tools.map.view

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.yaml.YamlParser
import java.awt.EventQueue
import javax.swing.JFrame

class MapViewer {

    init {
        EventQueue.invokeLater {
            LafManager.install(LafManager.themeForPreferredStyle(getPreferredThemeStyle()))
            val frame = JFrame("Map viewer")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            val cache = CacheDelegate("./data/cache/")
            val parser = YamlParser()
            val decoder = ObjectDecoder(cache, member = false, lowDetail = false)
            val defs = ObjectDefinitions(decoder).load(parser, "./data/definitions/objects.yml", null)
            val areas = Areas().load(parser, "./data/map/areas.yml")
            val nav = NavigationGraph(defs, areas).load(parser, "./data/map/nav-graph.yml")
            frame.add(MapView(nav, "./data/map/areas.yml"))
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    companion object {
        const val FILTER_VIEWPORT = false
        const val DISPLAY_CHUNKS = false
        @JvmStatic
        fun main(args: Array<String>) {
            MapViewer()
        }
    }
}