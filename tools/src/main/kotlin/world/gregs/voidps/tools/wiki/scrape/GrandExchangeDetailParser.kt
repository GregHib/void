package world.gregs.voidps.tools.wiki.scrape

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Dumps item details using the grand exchange api
 */
object GrandExchangeDetailParser {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val file = File("./GrandExchangeDetails-all.json")
        val text = file.readText()
        val mapper = ObjectMapper(JsonFactory())
        val array: Array<Map<String, Any>> = mapper.readValue(text)
        println(array.size)
        val ids = array.map {
            val map = it["item"] as Map<*, *>
            val id = map["id"] as Int
            id
        }.toSet()
        println(ids.toList())
    }

    fun parse(string: String) {
        val mapper = ObjectMapper(JsonFactory())
        val map: Map<String, Any> = mapper.readValue(string)
        val it = map["item"] as Map<*, *>
        val id = it["id"] as Int
        val category = it["type"] as String
        val name = it["name"] as String
        val description = it["description"] as String
        println("$id $category $name $description")
    }
}
