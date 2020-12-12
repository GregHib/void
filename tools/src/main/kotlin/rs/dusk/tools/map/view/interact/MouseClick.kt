package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.draw.GraphDrawer
import rs.dusk.tools.map.view.draw.HighlightedLink
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.tools.map.view.graph.Node
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

class MouseClick(
    private val view: MapView,
    private val nav: NavigationGraph,
    private val graph: GraphDrawer,
    private val link: HighlightedLink
) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        val mapX = view.viewToMapX(e.x)
        val mapY = view.flipMapY(view.viewToMapY(e.y))
        val node = nav.getNodeOrNull(mapX, mapY)
        if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
            toggleNode(node, mapX, mapY)
        } else if (SwingUtilities.isRightMouseButton(e) && node != null) {
            openNodeOptions(node, e)
        } else if (SwingUtilities.isRightMouseButton(e) && link.highlighted != null) {
            openLinkOptions(link.highlighted!!, e)
        }
    }

    private fun toggleNode(clicked: Node?, mapX: Int, mapY: Int) {
        var node = clicked
        if (node != null) {
            nav.removeNode(node)
            node.links.forEach {
                graph.repaint(it)
            }
        } else {
            node = nav.addNode(mapX, mapY)
        }
        graph.repaint(node)
    }

    private fun openNodeOptions(node: Node, e: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(JMenuItem("Edit node")).addActionListener {
            showNodeOptions()
        }
        popup.show(e.component, e.x, e.y)
    }

    private fun openLinkOptions(link: Link, e: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(JMenuItem("Edit link")).addActionListener {
        }
        popup.show(e.component, e.x, e.y)
    }

    private fun showNodeOptions() {

//        val result = JOptionPane.showConfirmDialog(null, panel, "Test",
//            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
//        if (result == JOptionPane.OK_OPTION) {
//            println("${bi.isEnabled} ${interaction.text} ${requirements.selectedValue}")
//        } else {
//            println("Cancelled")
//        }
//        val possibilities = arrayOf<Any>("ham", "spam", "yam")
//        val s = JOptionPane.showInputDialog(frame, "Complete the sentence:Green eggs and...", "Node edit", JOptionPane.PLAIN_MESSAGE, null, possibilities, "ham") as? String
//        println(s)
    }

}