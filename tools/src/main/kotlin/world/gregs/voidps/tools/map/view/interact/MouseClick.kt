package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.tools.map.view.draw.GraphDrawer
import world.gregs.voidps.tools.map.view.draw.HighlightedArea
import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.voidps.tools.map.view.graph.*
import world.gregs.voidps.tools.map.view.ui.AreaPointSettings
import world.gregs.voidps.tools.map.view.ui.AreaSettings
import world.gregs.voidps.tools.map.view.ui.LinkSettings
import world.gregs.voidps.tools.map.view.ui.NodeSettings
import world.gregs.voidps.type.Tile
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

class MouseClick(
    private val view: MapView,
    private val nav: MutableNavigationGraph,
    private val graph: GraphDrawer,
    private val area: HighlightedArea,
    private val areaSet: AreaSet,
) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        if (SwingUtilities.isRightMouseButton(e)) {
            val popup = JPopupMenu()
            val mapX = view.viewToMapX(e.x)
            val mapY = view.flipMapY(view.viewToMapY(e.y))
            val node = nav.getNodeOrNull(mapX, mapY, view.level)
            val links = if (node != null) nav.getLinks(node) else null
            val areas = area.highlighted
            val point = getPoint(areas, mapX, mapY)
            if (node == null) {
                popup.add(JMenuItem("Add node")).addActionListener {
                    graph.repaint(nav.addNode(mapX, mapY, view.level))
                }
            }
            popup.add(JMenuItem("Add area")).addActionListener {
                graph.repaint(areaSet.addArea(mapX, mapY, view.level))
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
                    val dx = link.end.x - link.start.x
                    val dy = link.end.y - link.start.y
                    val dz = link.end.level - link.start.level
                    view.offset(-dx, dy, dz)
                }
//                popup.add(JMenuItem("Remove link $index")).addActionListener {
//                    nav.removeLink(link)
//                }
            }
            if (point != null) {
                popup.add(JMenuItem("Remove point")).addActionListener {
                    areaSet.removePoint(point.area, point)
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
                        areaSet.removeArea(area)
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
        val result = JOptionPane.showConfirmDialog(
            null,
            settings,
            "Edit area",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
        )
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
            for (tag in tags) {
                settings.tagsList.addElement(tag)
            }
        }
    }

    private fun populate(area: Area, settings: AreaSettings) {
        val tags = settings.tagsList.toArray().filterIsInstance<String>().toList()
        area.tags = tags.ifEmpty { null }
    }

    private fun showPointSettings(area: Area, point: Point) {
        val settings = AreaPointSettings()
        populate(settings, point)
        val result = JOptionPane.showConfirmDialog(
            null,
            settings,
            "Edit area point",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
        )
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

    private fun showNodeSettings(node: Tile, links: List<Link>) {
        val settings = NodeSettings()
        populate(settings, node)
        val result = JOptionPane.showConfirmDialog(
            null,
            settings,
            "Edit node",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
        )
        if (result == JOptionPane.OK_OPTION) {
            val newNode = populate(node, settings)
            if (node != newNode) {
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

    private fun populate(settings: NodeSettings, node: Tile) {
        settings.coords.xCoord.text = node.x.toString()
        settings.coords.yCoord.text = node.y.toString()
    }

    private fun populate(node: Tile, settings: NodeSettings): Tile {
        val newX = settings.coords.xCoord.text.toIntOrNull() ?: node.x
        val newY = settings.coords.yCoord.text.toIntOrNull() ?: node.y
        val newZ = settings.coords.zCoord.text.toIntOrNull() ?: node.level
        return nav.updateNode(node, newX, newY, newZ)
    }

    private fun showLinkSettings(link: Link) {
        val settings = LinkSettings()
        populate(settings, link)
        val result = JOptionPane.showConfirmDialog(
            null,
            settings,
            "Edit link",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
        )
        if (result == JOptionPane.OK_OPTION) {
            populate(link, settings)
            graph.repaint(link)
        }
    }

    private fun populate(settings: LinkSettings, link: Link) {
        settings.start.xCoord.text = link.start.x.toString()
        settings.start.yCoord.text = link.start.y.toString()
        settings.start.zCoord.text = link.start.level.toString()
        settings.end.xCoord.text = link.end.x.toString()
        settings.end.yCoord.text = link.end.y.toString()
        settings.end.zCoord.text = link.end.level.toString()
        val actions = link.actions
        if (actions != null) {
            for (action in actions) {
                settings.actionsList.addElement(action)
            }
        }
        val requirements = link.requirements
        if (requirements != null) {
            for (requirement in requirements) {
                settings.requirementsList.addElement(requirement)
            }
        }
    }

    private fun populate(link: Link, settings: LinkSettings) {
        val actions = settings.actionsList.toArray().filterIsInstance<String>().toList()
        link.actions = actions.ifEmpty { null }
        val requirements = settings.requirementsList.toArray().filterIsInstance<String>().toList()
        link.requirements = requirements.ifEmpty { null }
    }
}
