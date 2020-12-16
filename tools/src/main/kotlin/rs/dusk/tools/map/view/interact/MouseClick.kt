package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.LinkSettings
import rs.dusk.tools.map.view.NodeSettings
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
        if (SwingUtilities.isRightMouseButton(e)) {
            val popup = JPopupMenu()
            popup.addNodeOptions(e)
            if (link.highlighted.isNotEmpty()) {
                popup.addLinkOptions(link.highlighted)
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
                link.update(e.x, e.y)
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

    /**
     * Add options to edit and remove links under mouse
     * Link index = counter clockwise from link highlighted
     */
    private fun JPopupMenu.addLinkOptions(links: List<Link>) {
        val single = links.size == 1
        for ((index, link) in links.withIndex()) {
            val i = if (single) "" else " ${index + 1}"
            add(JMenuItem("Edit link$i")).addActionListener {
                showLinkSettings(link)
            }
            add(JMenuItem("Go to link$i start")).addActionListener {
                view.centreOn(link.start.x, view.flipMapY(link.start.y))
            }
            add(JMenuItem("Go to link$i end")).addActionListener {
                view.centreOn(link.end.x, view.flipMapY(link.end.y))
            }
        }
        for ((index, link) in links.withIndex()) {
            val i = if (single) "" else " ${index + 1}"
            add(JMenuItem("Remove link$i")).addActionListener {
                showLinkSettings(link)
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
        settings.bidirectional.isSelected = link.bidirectional
        settings.interaction.text = link.interaction ?: ""
        val requirements = link.requirements
        if (requirements != null) {
            settings.requirementsList.addAll(requirements)
        }
    }

    private fun populate(link: Link, settings: LinkSettings) {
        link.bidirectional = settings.bidirectional.isSelected
        val interact = settings.interaction.text
        link.interaction = if (interact.isNotBlank()) interact else null
        val list = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (list.isNotEmpty()) list else null
    }

}