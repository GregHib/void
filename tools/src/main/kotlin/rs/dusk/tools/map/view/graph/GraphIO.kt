package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*

class GraphIO(private val nav: NavigationGraph) {
    private val file = java.io.File("./navgraph.json")
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
        val nodes = (map["nodes"] as List<Map<String, Any>>).map { Node(it["x"] as Int, it["y"] as Int) }
        val links = (map["links"] as List<Map<String, Any>>).map { Link(it["index"] as Int, it["index2"] as Int, it["bidirectional"] as Boolean, it["interaction"] as? String, it["requirements"] as? List<String>) }
        nav.nodes.addAll(nodes)
        links.forEach {
            it.node = nodes[it.index]
            it.node.links.add(it)
            it.node2 = nodes[it.index2]
            it.node2.links.add(it)
        }
        nav.links.addAll(links)
    }

    fun save() {
        if (!nav.changed) {
            return
        }
        writer.writeValue(file, mapOf(
            "nodes" to nav.nodes,
            "links" to nav.links
        ))
        nav.changed = false
    }
}