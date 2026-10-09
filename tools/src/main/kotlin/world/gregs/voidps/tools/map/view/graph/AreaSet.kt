package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config

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

        private fun points(name: String, x: IntList, y: IntList): MutableList<Point> {
            return when {
                x.size == y.size -> MutableList(x.size) { index -> Point(x.getInt(index), y.getInt(index)) }
                x.size == 1 && y.size == 2 -> mutableListOf(Point(x.getInt(0), y.getInt(0)), Point(x.getInt(0), y.getInt(1)))
                y.size == 1 && x.size == 2 -> mutableListOf(Point(x.getInt(0), y.getInt(0)), Point(x.getInt(1), y.getInt(0)))
                else -> throw IllegalArgumentException("Area '$name' has mismatched x/y sizes: x=${x.size}, y=${y.size}")
            }
        }

        fun load(paths: List<String>, set: AreaSet) {
            set.areas.clear()
            val areas = mutableListOf<Area>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val name = section()
                        val x = IntArrayList()
                        val y = IntArrayList()
                        var level: Int? = null
                        val tags = ObjectOpenHashSet<String>()
                        val params = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "x" -> while (nextElement()) {
                                    x.add(int())
                                }
                                "y" -> while (nextElement()) {
                                    y.add(int())
                                }
                                "level" -> level = int()
                                "tags" -> while (nextElement()) {
                                    tags.add(string())
                                }
                                else -> params[key] = value()
                            }
                        }
                        areas.add(
                            Area(
                                name,
                                level ?: 0,
                                level ?: 0,
                                points(name, x, y),
                            ),
                        )
                    }
                }
            }
            areas.forEach { area ->
                area.points.forEach {
                    it.area = area
                }
            }
            set.areas.addAll(areas)
        }
    }
}
