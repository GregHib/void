package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.*
import world.gregs.voidps.engine.map.Tile

class GraphIO(private val nav: NavigationGraph, private val area: AreaSet, path: String = "./navgraph.json") {
    private val file = java.io.File(path)
    private val reader = ObjectMapper(JsonFactory())
        .registerKotlinModule()
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

    private val writer = reader.writerWithDefaultPrettyPrinter()

    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            load()
            while (isActive) {
                delay(10000)
                save()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun load() {
        if (!file.exists()) {
            return
        }
        val map = reader.readValue<Map<String, Any>>(file)
        (map["links"] as Map<String, ArrayList<Map<String, Any>>>).forEach { (key, list) ->
            nav.adjacencyList[Tile(key.toInt())] = list.map {
                Link(Tile(it["start"] as Int),
                    Tile(it["end"] as Int),
                    it["actions"] as? List<String>,
                    it["requirements"] as? List<String>
                )
            }.toMutableList()
        }
        val areas = (map["areas"] as? List<Map<String, Any>>)?.map {
            Area(
                it["name"] as? String,
                it["planeMin"] as Int,
                it["planeMax"] as Int,
                (it["points"] as List<Map<String, Any>>).map { p ->
                    Point(
                        p["x"] as Int,
                        p["y"] as Int
                    )
                }.toMutableList()
            )
        }
        if (areas != null) {
            areas.forEach { area ->
                area.points.forEach {
                    it.area = area
                }
            }
            area.areas.addAll(areas)
        }
    }

    fun save() {
        if (!nav.changed) {
            return
        }
        writer.writeValue(file, mapOf(
            "links" to nav.adjacencyList.mapKeys { it.key.id },
            "areas" to area.areas
        ))
        nav.changed = false
    }
}