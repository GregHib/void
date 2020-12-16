package rs.dusk.tools.map.view.graph

class NavigationGraph {

    val links = mutableListOf<Link>()
    val areas = mutableSetOf<Area>()
    var changed = false

    fun addLink(x: Int, y: Int, z: Int): Link {
        val link = Link(x, y, z)
        addLink(link)
        return link
    }

    private fun addLink(link: Link) {
        links.add(link)
        changed = true
    }

    fun removeLink(link: Link) {
        links.remove(link)
        changed = true
    }

    fun getLinkOrNull(x: Int, y: Int, z: Int) = links.firstOrNull { it.x == x && it.y == y && it.z == z }

    fun getPointOrNull(x: Int, y: Int, z: Int): Point? {
        for (area in areas) {
            if (area.plane != z) {
                continue
            }
            return area.points.firstOrNull { it.x == x && it.y == y } ?: continue
        }
        return null
    }

    fun addArea(x: Int, y: Int, z: Int): Area {
        val area = Area(null, z, mutableListOf())
        addPoint(area, x, y)
        areas.add(area)
        changed = true
        return area
    }

    fun addPoint(area: Area, x: Int, y: Int) {
        val point = Point(x, y)
        area.points.add(point)
        point.area = area
        changed = true
    }

    fun addPoint(after: Point, x: Int, y: Int) {
        val area = after.area
        val index = area.points.indexOf(after) + 1
        val point = Point(x, y)
        area.points.add(index, point)
        point.area = area
        changed = true
    }

    fun updateLink(original: Link, link: Link) {
        links[links.indexOf(original)] = link
        changed = true
    }

    fun removeArea(area: Area) {
        areas.remove(area)
        changed = true
    }

    fun removePoint(area: Area, point: Point) {
        area.points.remove(point)
        changed = true
    }

}