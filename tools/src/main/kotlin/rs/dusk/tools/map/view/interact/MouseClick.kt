package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.LinkSettings
import rs.dusk.tools.map.view.draw.GraphDrawer
import rs.dusk.tools.map.view.draw.HighlightedArea
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
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
            popup.addLinkOptions(e)
            if (area.highlighted.isNotEmpty()) {
                popup.addAreaOptions(area.highlighted)
            }
            popup.show(e.component, e.x, e.y)
        }
    }

    private fun JPopupMenu.addLinkOptions(e: MouseEvent) {
        val mapX = view.viewToMapX(e.x)
        val mapY = view.flipMapY(view.viewToMapY(e.y))
        val link = nav.getLinkOrNull(mapX, mapY, 0)
        if (link != null) {
            add(JMenuItem("Remove link")).addActionListener {
                nav.removeLink(link)
                area.update(e.x, e.y)
                graph.repaint(link)
            }
            add(JMenuItem("Edit link")).addActionListener {
                showLinkSettings(link)
            }
        } else {
            add(JMenuItem("Add link")).addActionListener {
                graph.repaint(nav.addLink(mapX, mapY, 0))
            }
        }
    }

    private fun JPopupMenu.addAreaOptions(e: MouseEvent) {
        val mapX = view.viewToMapX(e.x)
        val mapY = view.flipMapY(view.viewToMapY(e.y))
        add(JMenuItem("Add area")).addActionListener {
            graph.repaint(nav.addArea(mapX, mapY, 0))
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
                nav.removeArea(area)
            }
        }
    }

    private fun showLinkSettings(link: Link) {
        val settings = LinkSettings()
        populate(settings, link)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit link",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            val newLink = populate(link, settings)
            nav.updateLink(link, newLink)
            graph.repaint(link)
            graph.repaint(newLink)
        }
    }

    private fun populate(settings: LinkSettings, link: Link) {
        settings.xCoord.text = link.x.toString()
        settings.yCoord.text = link.y.toString()
        settings.zCoord.text = link.z.toString()
        val actions = link.actions
        if (actions != null) {
            settings.actionsList.addAll(actions)
        }
        val requirements = link.requirements
        if (requirements != null) {
            settings.requirementsList.addAll(requirements)
        }
    }

    private fun populate(original: Link, settings: LinkSettings): Link {
        val link = Link(
            x = settings.xCoord.text.toIntOrNull() ?: original.x,
            y = settings.yCoord.text.toIntOrNull() ?: original.y,
            z = settings.zCoord.text.toIntOrNull() ?: original.z
        )
        val actions = settings.actionsList.toArray().filterIsInstance<String>().toList()
        link.actions = if (actions.isNotEmpty()) actions else null
        val requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (requirements.isNotEmpty()) requirements else null
        return link
    }

}