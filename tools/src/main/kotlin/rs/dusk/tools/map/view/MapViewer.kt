package rs.dusk.tools.map.view

import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

class MapViewer {

    init {
        EventQueue.invokeLater {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (ex: ClassNotFoundException) {
                ex.printStackTrace()
            } catch (ex: InstantiationException) {
                ex.printStackTrace()
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            } catch (ex: UnsupportedLookAndFeelException) {
                ex.printStackTrace()
            }
            val frame = JFrame("Testing")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.add(MapView())
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