package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.tools.map.view.draw.GraphDrawer
import world.gregs.voidps.tools.map.view.draw.HighlightedArea
import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.voidps.tools.map.view.graph.Area
import world.gregs.voidps.tools.map.view.graph.Link
import world.gregs.voidps.tools.map.view.graph.NavigationGraph
import world.gregs.voidps.tools.map.view.graph.Point
import world.gregs.voidps.tools.map.view.ui.AreaPointSettings
import world.gregs.voidps.tools.map.view.ui.AreaSettings
import world.gregs.voidps.tools.map.view.ui.LinkSettings
import world.gregs.voidps.tools.map.view.ui.NodeSettings
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
            val node = nav.getNodeOrNull(mapX, mapY, view.plane)
            val links = if (node != null) nav.getLinks(node) else null
            val areas = area.highlighted
            val point = getPoint(areas, mapX, mapY)
            if (node == null) {
                popup.add(JMenuItem("Add node")).addActionListener {
                    graph.repaint(nav.addNode(mapX, mapY, view.plane))
                }
            }
            popup.add(JMenuItem("Add area")).addActionListener {
                graph.repaint(nav.addArea(mapX, mapY, view.plane))
            }
            if (node != null && links != null) {
                popup.add(JMenuItem("Edit node")).addActionListener {
                    showNodeSettings(node, links)
                }
            }
            if (point != null) {
                popup.add(JMenuItem("Edit point")).addActionListener {
                    showPointSettings(point.area, point)
                }
            }
            if (areas.isNotEmpty()) {
                for ((index, area) in areas.withIndex()) {
                    popup.add(JMenuItem("Edit area${if (index > 0) (index + 1).toString() else ""}")).addActionListener {
                        showAreaSettings(area)
                    }
                }
            }
            links?.forEachIndexed { index, link ->
                popup.add(JMenuItem("Edit link $index")).addActionListener {
                    showLinkSettings(link)
                }
                popup.add(JMenuItem("Go to link $index target")).addActionListener {
                    val dx = link.tx - link.x
                    val dy = link.ty - link.y
                    val dz = link.tz - link.z
                    view.offset(-dx, dy, dz)
                }
                popup.add(JMenuItem("Remove link $index")).addActionListener {
                    nav.removeLink(link)
                }
            }
            if (point != null) {
                popup.add(JMenuItem("Remove point")).addActionListener {
                    nav.removePoint(point.area, point)
                    graph.repaint(point.area)
                }
            }
            if (node != null && links != null) {
                popup.add(JMenuItem("Remove node")).addActionListener {
                    nav.removeNode(node)
                    links.forEach {
                        nav.removeLink(it)
                        graph.repaint(it)
                    }
                    nav.getLinked(node).forEach {
                        nav.removeLink(it)
                        graph.repaint(it)
                    }
                    area.update(e.x, e.y)
                    graph.repaint(node)
                }
            }
            if (areas.isNotEmpty()) {
                for ((index, area) in areas.withIndex()) {
                    popup.add(JMenuItem("Remove area${if (index > 0) (index + 1).toString() else ""}")).addActionListener {
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

    private fun showAreaSettings(area: Area) {
        val settings = AreaSettings()
        populate(settings, area)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit area",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            populate(area, settings)
            nav.changed = true
            graph.repaint(area)
        }
    }

    private fun populate(settings: AreaSettings, area: Area) {
        settings.name.text = area.name ?: ""
        val tags = area.tags
        if (tags != null) {
            settings.tagsList.addAll(tags)
        }
    }

    private fun populate(area: Area, settings: AreaSettings) {
        val tags = settings.tagsList.toArray().filterIsInstance<String>().toList()
        area.tags = if (tags.isNotEmpty()) tags else null
    }

    private fun showPointSettings(area: Area, point: Point) {
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

    private fun showNodeSettings(node: Int, links: List<Link>) {
        val settings = NodeSettings()
        populate(settings, node)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit node",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            val newNode = populate(node, settings)
            if(node != newNode) {
                links.forEach {
                    graph.repaint(it)
                }
                graph.repaint(node)
                nav.getLinks(newNode).forEach {
                    graph.repaint(it)
                }
                graph.repaint(newNode)
            }
        }
    }

    private fun populate(settings: NodeSettings, node: Int) {
        settings.coords.xCoord.text = Tile.getX(node).toString()
        settings.coords.yCoord.text = Tile.getY(node).toString()
    }

    private fun populate(node: Int, settings: NodeSettings): Int {
        val newX = settings.coords.xCoord.text.toIntOrNull() ?: Tile.getX(node)
        val newY = settings.coords.yCoord.text.toIntOrNull() ?: Tile.getY(node)
        val newZ = settings.coords.zCoord.text.toIntOrNull() ?: Tile.getPlane(node)
        return nav.updateNode(node, newX, newY, newZ)
    }

    private fun showLinkSettings(link: Link) {
        val settings = LinkSettings()
        populate(settings, link)
        val result = JOptionPane.showConfirmDialog(null, settings, "Edit link",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (result == JOptionPane.OK_OPTION) {
            populate(link, settings)
            graph.repaint(link)
        }
    }

    private fun populate(settings: LinkSettings, link: Link) {
        settings.start.xCoord.text = link.x.toString()
        settings.start.yCoord.text = link.y.toString()
        settings.start.zCoord.text = link.z.toString()
        settings.end.xCoord.text = link.tx.toString()
        settings.end.yCoord.text = link.ty.toString()
        settings.end.zCoord.text = link.tz.toString()
        val actions = link.actions
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
        link.actions = if (actions.isNotEmpty()) actions else null
        val requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = if (requirements.isNotEmpty()) requirements else null
    }

}