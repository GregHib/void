package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.LinkSettings
import rs.dusk.tools.map.view.draw.GraphDrawer
import rs.dusk.tools.map.view.draw.HighlightedLink
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.tools.map.view.graph.Node
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JOptionPane
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
        }
        popup.show(e.component, e.x, e.y)
    }

    private fun openLinkOptions(link: Link, e: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(JMenuItem("Edit link")).addActionListener {
            showLinkSettings(link)
        }
        popup.show(e.component, e.x, e.y)
    }

    private fun showLinkSettings(link: Link) {
        val settings = LinkSettings()
        populate(settings, link)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit link",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            populate(link, settings)
        }
    }

    private fun populate(settings: LinkSettings, link: Link) {
        settings.bidirectional.isSelected = link.bidirectional
        settings.interaction.text = link.interaction ?: ""
        val requirements = link.requirements
        if (requirements != null) {
            settings.requirementsList.addAll(requirements)
        }
    }

    private fun populate(link: Link, settings: LinkSettings) {
        link.bidirectional = settings.bidirectional.isSelected
        link.interaction = settings.interaction.text
        link.requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
    }

}