package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.LinkSettings
import rs.dusk.tools.map.view.NodeSettings
import rs.dusk.tools.map.view.draw.GraphDrawer
import rs.dusk.tools.map.view.draw.HighlightedArea
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.graph.Area
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
    private val area: HighlightedArea
) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        if (SwingUtilities.isRightMouseButton(e)) {
            val popup = JPopupMenu()
            popup.addNodeOptions(e)
            if (area.highlighted.isNotEmpty()) {
                popup.addAreaOptions(area.highlighted)
            }
            popup.show(e.component, e.x, e.y)
        }
    }

    private fun JPopupMenu.addNodeOptions(e: MouseEvent) {
        val mapX = view.viewToMapX(e.x)
        val mapY = view.flipMapY(view.viewToMapY(e.y))
        val node = nav.getNodeOrNull(mapX, mapY, 0)
        if (node != null) {
            add(JMenuItem("Remove node")).addActionListener {
                nav.removeNode(node)
                node.links.forEach {
                    graph.repaint(it)
                }
                area.update(e.x, e.y)
                graph.repaint(node)
            }
            add(JMenuItem("Edit node")).addActionListener {
                showNodeSettings(node)
            }
        } else {
            add(JMenuItem("Add node")).addActionListener {
                graph.repaint(nav.addNode(mapX, mapY, 0))
            }
        }
    }

    private fun JPopupMenu.addAreaOptions(areas: List<Area>) {
        val single = areas.size == 1
        for ((index, area) in areas.withIndex()) {
            val i = if (single) "" else " ${index + 1}"
            add(JMenuItem("Edit area$i")).addActionListener {
            }
        }
        for ((index, area) in areas.withIndex()) {
            val i = if (single) "" else " ${index + 1}"
            add(JMenuItem("Remove area$i")).addActionListener {
            }
        }
    }

    private fun showNodeSettings(node: Node) {
        val settings = NodeSettings()
        populate(settings, node)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit node",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            val newNode = populate(node, settings)
            nav.updateNode(node, newNode)
            graph.repaint(node)
            graph.repaint(newNode)
        }
    }

    private fun populate(settings: NodeSettings, node: Node) {
        settings.xCoord.text = node.x.toString()
        settings.yCoord.text = node.y.toString()
        settings.zCoord.text = node.z.toString()
    }

    private fun populate(node: Node, settings: NodeSettings): Node {
        return Node(settings.xCoord.text.toIntOrNull() ?: node.x, settings.yCoord.text.toIntOrNull() ?: node.y, settings.zCoord.text.toIntOrNull() ?: node.z)
    }

    private fun showLinkSettings(link: Link) {
        val settings = LinkSettings()
        populate(settings, link)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit link",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            populate(link, settings)
            nav.changed = true
        }
    }

    private fun populate(settings: LinkSettings, link: Link) {
        val actions = link.requirements// FIXME
        if (actions != null) {
            settings.actionsList.addAll(actions)
        }
        val requirements = link.requirements
        if (requirements != null) {
            settings.requirementsList.addAll(requirements)
        }
    }

    private fun populate(link: Link, settings: LinkSettings) {
        val actions = settings.actionsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (actions.isNotEmpty()) actions else null// FIXME
        val requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (requirements.isNotEmpty()) requirements else null
    }

}