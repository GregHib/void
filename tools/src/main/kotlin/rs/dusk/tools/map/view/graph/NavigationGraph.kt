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

    fun getAreaOrNull(x: Int, y: Int, z: Int) = areas.firstOrNull {
        it.plane == z && it.points.any { p -> p.x == x && p.y == y }
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

    fun updateLink(original: Link, link: Link) {
        links[links.indexOf(original)] = link
        changed = true
    }

    fun removeArea(area: Area) {
        areas.remove(area)
        changed = true
    }

}