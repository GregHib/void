package world.gregs.void.tools.map.view

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import world.gregs.void.tools.map.view.draw.MapView
import java.awt.EventQueue
import javax.swing.JFrame

class MapViewer {

    init {
        EventQueue.invokeLater {
            LafManager.install(LafManager.themeForPreferredStyle(getPreferredThemeStyle()))
            val frame = JFrame("Map viewer")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.add(MapView("./navgraph.json"))
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MapViewer()
        }
    }
}