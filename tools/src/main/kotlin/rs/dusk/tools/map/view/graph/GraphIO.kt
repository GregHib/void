package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*

class GraphIO(private val nav: NavigationGraph, path: String = "./navgraph.json") {
    private val file = java.io.File(path)
    private val reader = ObjectMapper(JsonFactory())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

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
        val links = (map["links"] as List<Map<String, Any>>).map { Link(it["x"] as Int, it["y"] as Int, it["z"] as Int, it["interaction"] as? List<String>, it["requirements"] as? List<String>) }
        val areas = (map["areas"] as? List<Map<String, Any>>)?.map { Area(it["name"] as? String, it["plane"] as Int, (it["points"] as List<Map<String, Any>>).map { p -> Point(p["x"] as Int, p["y"] as Int) }.toMutableList()) }
        nav.links.addAll(links)
        if (areas != null) {
            nav.areas.addAll(areas)
        }
    }

    fun save() {
        if (!nav.changed) {
            return
        }
        writer.writeValue(file, mapOf(
            "links" to nav.links,
            "areas" to nav.areas
        ))
        nav.changed = false
    }
}