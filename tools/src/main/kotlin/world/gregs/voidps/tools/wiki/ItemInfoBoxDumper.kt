package world.gregs.voidps.tools.wiki

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import world.gregs.voidps.tools.wiki.model.Wiki
import java.io.File

object ItemInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("Items.json")
        val mapper = ObjectMapper(JsonFactory())

        val items = mutableMapOf<String, String>()

        val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\Old+School+RuneScape+Wiki-20210824214429.xml")

        val stop = false
        val names = mutableListOf<String>()
        wiki.pages.filter { it.namespace.key == 0 }.forEach { page ->
            if (stop) {
                return@forEach
            }
            val text = page.revision.text
            if (text.contains("ItemSpawnLine", true)) {
                val list = page.getTemplateMaps("ItemSpawnLine")
                for (map in list) {
                    if (!map.containsKey("")) {
//                        println(map)
                        continue
                    }
                    val coords = map[""] as String
                    val parts = coords.split(",")
                    val x = parts.firstOrNull() ?: -1
                    val y = parts.getOrNull(1) ?: -1
                    var quantity = parts.getOrNull(2) ?: "1"
                    if (quantity.contains(":")) {
                        quantity = quantity.split(":").last()
                    }
                    var respawn = parts.getOrNull(3) ?: "-1"
                    if (respawn.contains(":")) {
                        respawn = respawn.split(":").last()
                    }
                    if (respawn.contains("(on table)")) {
                        respawn = parts[4].split(":").last()
                    }
                    println("$x,$y,$quantity,$respawn")
//                    if (coords.contains("qty:")) {
//                        val parts = coords.split(",qty:")
//                        coords = parts.first()
//                        quantity = parts[1].toInt()
//                    }
//                    for ((key, value) in map) {
//                        println("$key=$value")
//                    }
                }
            }
            if (text.contains("infobox item", true)) {
                val map = page.getTemplateMaps("infobox item").first()
                if (map.containsKey("id")) {
                    println("${map["id"]},${map["respawn"]}")
                } else if (map.containsKey("id1")) {
                    repeat(4) {
                        if (map.containsKey("id$it")) {
                            println("${map["id$it"]},${map["respawn"]}")
                        }
                    }
                } else {
                    names.add("${page.title},${map["respawn"]}")
                }
                println()
            }
        }

        names.forEach {
            println(it)
        }
//        mapper.writeValue(file, items)
    }
}
