package world.gregs.voidps.tools.wiki.scrape

import world.gregs.voidps.tools.wiki.model.Wiki

@Suppress("UNCHECKED_CAST")
internal object RunescapeWikiAutoExporter {

    @JvmStatic
    fun main(args: Array<String>) {
        /*val wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")

        val names = mutableListOf<String>()
        wiki.pages.filter { it.namespace.key == 0 }.forEach { page ->
            val text = page.revision.text
            if (text.contains("infobox item", true)) {
                names.add(page.title)
            }
        }*/

        val currentWiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\RuneScape+Wiki-20201106190455.xml")
        val regex = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()
        val ids = mutableMapOf<Int, String>()
        currentWiki.pages.forEach { page ->
            if (page.revision.text.contains("infobox item", true)) {
                val template = page.templates.firstOrNull { it.first.contains("infobox item", true) } ?: return@forEach
                val map = template.second as Map<String, Any>
                if (map.containsKey("id")) {
                    val value = map["id"] as? String
                    if (value != null) {
                        if (value.contains(",")) {
                            value.split(",").forEach {
                                ids[it.trim().toInt()] = page.title
                            }
                        } else if (value.isNotBlank()) {
                            ids[value.toInt()] = page.title
                        }
                    }
                } else if (map.containsKey("id1")) {
                    ids[(map["id1"] as String).toInt()] = page.title
                } else if (map.containsKey("id2")) {
                    ids[(map["id2"] as String).toInt()] = page.title
                } else if (map.containsKey("id3")) {
                    ids[(map["id3"] as String).toInt()] = page.title
                } else if (map.containsKey("id4")) {
                    ids[(map["id4"] as String).toInt()] = page.title
                } else if (!map.containsKey("rscid")) {
                    println("No id $map")
                }
            }
        }
        println(ids.size)
    }
}
