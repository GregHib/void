package world.gregs.voidps.tools.map.view

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.tools.map.view.draw.MapView
import java.awt.EventQueue
import javax.swing.JFrame

class MapViewer {

    init {
        EventQueue.invokeLater {
            LafManager.install(LafManager.themeForPreferredStyle(getPreferredThemeStyle()))
            val frame = JFrame("Map viewer")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            val cache = CacheDelegate("./data/cache/")
            val storage = FileStorage()
            val decoder = ObjectDecoder(cache, member = false, lowDetail = false, configReplace = false)
            val defs = ObjectDefinitions(decoder).load(storage, "./data/definitions/objects.yml")
            val areas = Areas().load(storage, "./data/map/areas.yml")
            val nav = NavigationGraph(defs, areas).load("./data/map/nav-graph.yml")
            frame.add(MapView(nav, "./data/map/areas.yml"))
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    companion object {
        const val DISPLAY_CHUNKS = false
        @JvmStatic
        fun main(args: Array<String>) {
            MapViewer()
        }
    }
}