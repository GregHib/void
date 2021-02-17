package world.gregs.voidps.tools.map.view.graph

class AreaSet {
    val areas = mutableSetOf<Area>()
    var changed = false

    fun getPointOrNull(x: Int, y: Int, z: Int): Point? {
        for (area in areas) {
            if (z !in area.planes) {
                continue
            }
            return area.points.firstOrNull { it.x == x && it.y == y } ?: continue
        }
        return null
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

    fun removePoint(area: Area, point: Point) {
        area.points.remove(point)
        changed = true
    }

    fun addArea(x: Int, y: Int, z: Int): Area {
        val area = Area(null, z, 3, mutableListOf())
        addPoint(area, x, y)
        areas.add(area)
        changed = true
        return area
    }

    fun removeArea(area: Area) {
        areas.removeIf { it.planes == area.planes && it.minX == area.minX && it.minY == area.minY && it.maxX == area.maxX && it.maxY == area.maxY }
        changed = true
    }
}