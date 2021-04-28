package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader

class AreaLoader(
    private val loader: FileLoader
) : TimedLoader<Areas>("area") {
    override fun load(args: Array<out Any?>): Areas {
        val data: Map<String, Map<String, Any>> = loader.load(args.first() as String)
        val areas = data.mapValues { (key, value) -> toArea(key, value) }
        val tagged = mutableMapOf<String, MutableSet<MapArea>>()
        for (key in data.keys) {
            val area = areas.getValue(key)
            for (tag in area.values.keys) {
                tagged.getOrPut(tag) { mutableSetOf() }.add(area)
            }
        }
        count = areas.size
        return Areas(areas, tagged)
    }

    private fun toArea(name: String, map: Map<String, Any>): MapArea {
        val area = map["area"] as Map<String, Any>
        val x = area["x"] as List<Int>
        val y = area["y"] as List<Int>
        val plane = area["plane"] as? Int ?: 0
        val shape = if (x.size <= 2) {
            Rectangle(x.first(), y.first(), x.last(), y.last(), plane)
        } else {
            Polygon(x.toIntArray(), y.toIntArray(), plane)
        }
        return MapArea(name, shape, map["values"] as? Map<String, Any> ?: emptyMap())
    }
}