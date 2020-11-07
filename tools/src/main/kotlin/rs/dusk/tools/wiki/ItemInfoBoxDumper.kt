package rs.dusk.tools.wiki

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import rs.dusk.tools.wiki.model.Wiki
import java.io.File

object ItemInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {

        val file = File("Items.json")
        val mapper = ObjectMapper(JsonFactory())

        val items = mutableMapOf<String, String>()

        val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")

        var stop = false
        val names = mutableListOf<String>()
        wiki.pages.filter { it.namespace.key == 0 }.forEach { page ->
            if(stop) {
                return@forEach
            }
            val text = page.revision.text
            if (text.contains("infobox item", true)) {
                names.add(page.title)
//                val template = page.templates.firstOrNull { it.first.contains("infobox item", true) }
//                if (template != null) {
//                    items[page.title] = getWikiText(page.title)
//                    println("Scraped ${page.title}")
//                    if(items.size > 10) {
//                        stop = true
//                    }
//                } else {
//                    println("Cant find ${page.title} ${page.templates.map { it.first }}")
//                }
            }
        }


        println(names.size)
        names.forEach {
            println(it)
        }
//        mapper.writeValue(file, items)

    }

}