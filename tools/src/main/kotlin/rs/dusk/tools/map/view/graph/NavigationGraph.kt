package rs.dusk.tools.map.view.graph

class NavigationGraph {

    val links = mutableSetOf<Link>()
    val areas = mutableSetOf<Area>()
    var changed = false

    fun addLink(x: Int, y: Int, z: Int): Link {
        val link = Link(x, y, z)
        addLink(link)
        return link
    }

    fun addLink(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int): Link {
        val link = addLink(x, y, z)
        link.dx = x2 - x
        link.dy = y2 - y
        link.dz = z2 - z
        return link
    }

    fun createLink(x: Int, y: Int, z: Int) = getLinkOrNull(x, y, z) ?: addLink(x, y, z)

    fun createLink(x: Int, y: Int, z: Int, dx: Int, dy: Int, dz: Int): Link {
        val link = getLinkOrNull(x, y, z, dx, dy, dz) ?: addLink(x, y, z)
        link.dx = dx
        link.dy = dy
        link.dz = dz
        return link
    }

    fun createJointLink(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int): Link {
        val link = getJointLinkOrNull(x, y, z, x2, y2, z2) ?: addLink(x, y, z)
        link.dx = x2 - x
        link.dy = y2 - y
        link.dz = z2 - z
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

    fun getLinkOrNull(x: Int, y: Int, z: Int, dx: Int, dy: Int, dz: Int) = links.firstOrNull { it.x == x && it.y == y && it.z == z && it.dx == dx && it.dy == dy && it.dz == dz }

    fun getJointLinkOrNull(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) = links.firstOrNull { it.x == x && it.y == y && it.z == z && it.dx == x2 - x && it.dy == y2 - y && it.dz == z2 - z }

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
        links.remove(original)
        links.add(link)
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