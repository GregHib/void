package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import world.gregs.yaml.Yaml

class AreaSet {
    val areas = mutableSetOf<Area>()
    private var changed = false

    fun getPointOrNull(x: Int, y: Int, z: Int): Point? {
        for (area in areas) {
            if (z !in area.levels) {
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
        areas.removeIf { it.levels == area.levels && it.minX == area.minX && it.minY == area.minY && it.maxX == area.maxX && it.maxY == area.maxY }
        changed = true
    }

    companion object {
        private val reader = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        private val writer = reader.writerWithDefaultPrettyPrinter()

        fun save(set: AreaSet, path: String = "./areas.json") {
//            writer.writeValue(File(path), set.areas)
        }

        @Suppress("UNCHECKED_CAST")
        fun load(yaml: Yaml, path: String = "./areas.toml"): AreaSet {
            val set = AreaSet()
            val map = yaml.load<Map<String, Map<String, Any>>>(path)
            val areas = map.map { (key, value) ->
                val a = value["area"] as Map<String, Any>
                val x = a["x"] as List<Int>
                val y = a["y"] as List<Int>
                Area(
                    key,
                    value["level"] as? Int ?: 0,
                    value["level"] as? Int ?: 0,
                    x.mapIndexed { index, m -> Point(m, y[index]) }.toMutableList()
                )
            }
            areas.forEach { area ->
                area.points.forEach {
                    it.area = area
                }
            }
            set.areas.addAll(areas)
            return set
        }
    }
}