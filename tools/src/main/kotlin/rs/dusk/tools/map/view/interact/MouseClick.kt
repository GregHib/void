package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.draw.GraphDrawer
import rs.dusk.tools.map.view.draw.HighlightedArea
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.tools.map.view.graph.Point
import rs.dusk.tools.map.view.ui.AreaPointSettings
import rs.dusk.tools.map.view.ui.LinkSettings
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
            val mapX = view.viewToMapX(e.x)
            val mapY = view.flipMapY(view.viewToMapY(e.y))
            val link = nav.getLinkOrNull(mapX, mapY, view.plane)
            val areas = area.highlighted
            val point = getPoint(areas, mapX, mapY)
            if (link == null) {
                popup.add(JMenuItem("Add link")).addActionListener {
                    graph.repaint(nav.addLink(mapX, mapY, view.plane))
                }
            }
            if (areas.isEmpty()) {
                popup.add(JMenuItem("Add area")).addActionListener {
                    graph.repaint(nav.addArea(mapX, mapY, view.plane))
                }
            }
            if (link != null) {
                popup.add(JMenuItem("Edit link")).addActionListener {
                    showLinkSettings(link)
                }
                popup.add(JMenuItem("Go to link target")).addActionListener {
                    view.offset(link.dx, link.dy, link.dz)
                }
            }
            if (point != null) {
                popup.add(JMenuItem("Edit point")).addActionListener {
                    showAreaSettings(point.area, point)
                }
            }
            if (point != null) {
                popup.add(JMenuItem("Remove point")).addActionListener {
                    nav.removePoint(point.area, point)
                    graph.repaint(point.area)
                }
            }
            if (link != null) {
                popup.add(JMenuItem("Remove link")).addActionListener {
                    nav.removeLink(link)
                    area.update(e.x, e.y)
                    graph.repaint(link)
                }
            }
            if (areas.isNotEmpty()) {
                for ((index, area) in areas.withIndex()) {
                    popup.add(JMenuItem("Remove area${if (index > 0) (index + 1).toString() else ""}")).addActionListener {
                        println("Remove area $area")
                        nav.removeArea(area)
                        graph.repaint(area)
                    }
                }
            }
            popup.show(e.component, e.x, e.y)
        }
    }

    private fun getPoint(areas: List<Area>, x: Int, y: Int): Point? {
        for (area in areas) {
            return area.points.firstOrNull { it.x == x && it.y == y } ?: continue
        }
        return null
    }

    private fun showAreaSettings(area: Area, point: Point) {
        val settings = AreaPointSettings()
        populate(settings, point)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit area point",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            populate(point, settings)
            nav.changed = true
            graph.repaint(area)
        }
    }

    private fun populate(settings: AreaPointSettings, point: Point) {
        settings.coords.xCoord.text = point.x.toString()
        settings.coords.yCoord.text = point.y.toString()
    }

    private fun populate(point: Point, settings: AreaPointSettings) {
        point.x = settings.coords.xCoord.text.toIntOrNull() ?: point.x
        point.y = settings.coords.yCoord.text.toIntOrNull() ?: point.y
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
        settings.coords.xCoord.text = link.x.toString()
        settings.coords.yCoord.text = link.y.toString()
        settings.coords.zCoord.text = link.z.toString()
        settings.delta.xCoord.text = link.dx.toString()
        settings.delta.yCoord.text = link.dy.toString()
        settings.delta.zCoord.text = link.dz.toString()
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
            x = settings.coords.xCoord.text.toIntOrNull() ?: original.x,
            y = settings.coords.yCoord.text.toIntOrNull() ?: original.y,
            z = settings.coords.zCoord.text.toIntOrNull() ?: original.z,
            dx = settings.delta.xCoord.text.toIntOrNull() ?: original.dx,
            dy = settings.delta.yCoord.text.toIntOrNull() ?: original.dy,
            dz = settings.delta.zCoord.text.toIntOrNull() ?: original.dz
        )
        val actions = settings.actionsList.toArray().filterIsInstance<String>().toList()
        link.actions = if (actions.isNotEmpty()) actions else null
        val requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (requirements.isNotEmpty()) requirements else null
        return link
    }

}